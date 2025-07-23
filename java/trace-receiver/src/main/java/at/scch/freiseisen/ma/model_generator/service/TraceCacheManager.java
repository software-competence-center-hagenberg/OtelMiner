package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.commons.RestPageImpl;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import at.scch.freiseisen.ma.model_generator.error.TraceCacheException;
import at.scch.freiseisen.ma.model_generator.model.Seed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class TraceCacheManager implements DisposableBean {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final PersistenceService persistenceService;
    private final LinkedBlockingQueue<Trace> queue;
    private final ExecutorService executor;
    private final AtomicBoolean isPaused;
    private final AtomicBoolean isDead;
    private final AtomicBoolean isSeeded;
    private SourceDetails sourceDetails;
    private String probDeclareId;

    public TraceCacheManager(RestConfig restConfig, RestTemplate restTemplate, PersistenceService persistenceService) {
        this.restConfig = restConfig;
        this.restTemplate = restTemplate;
        this.persistenceService = persistenceService;
        queue = new LinkedBlockingQueue<>();
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "TraceCacheManager-Worker");
            thread.setUncaughtExceptionHandler((t, e) ->
                    log.error("Uncaught exception in cache thread: {}", e.getMessage(), e));
            return thread;
        });
        isPaused = new AtomicBoolean(false);
        isDead = new AtomicBoolean(false);
        isSeeded = new AtomicBoolean(false);
    }

    @Override
    public void destroy() {
        kill();
        executor.shutdownNow();
    }

    public void pause() {
        if (isDead.get()) {
            throw new TraceCacheException("Cannot pause the cache because the cache is dead");
        }
        isPaused.set(true);
    }

    public void resume() {
        if (isSeeded.get()) {
            log.info("cache is seeded -> not retrieving pages");
            return;
        }
        if (isDead.get()) {
            throw new TraceCacheException("Cannot resume the cache because the cache is dead");
        }
        isPaused.set(false);
        executor.submit(this::cache);
    }

    public void kill() {
        isDead.set(true);
        queue.clear();
    }

    public void start(@NonNull SourceDetails sourceDetails, @NonNull String probDeclareId) {
        if (isPaused.getAcquire()) {
            throw new TraceCacheException("Cannot start the cache because the cache is paused --> use resume!");
        }
        if (!isDead.getAcquire()) {
            kill();
        }
        log.info("Starting trace cache manager");
        this.probDeclareId = probDeclareId;
        this.sourceDetails = sourceDetails;
        queue.clear();
        isDead.set(false);
        executor.submit(this::cache);
    }

    public Trace take() throws InterruptedException {
        log.info("retrieving trace from cache");
        Trace trace = queue.take();
        if (!isSeeded.get()) {
            persistenceService.createAndPersistProbDeclareToTrace(probDeclareId, List.of(trace));
        }
        return trace;
    }

    /**
     * <pre>
     *     Retrieves up to n elements from the queue. The first element is retrieved with a blocking call,
     *     the rest non-blocking.
     *     NOTE: this does not create ProbDeclareToTrace entries and is currently only used for removing the first
     *           n elements of a page which already have been processed!
     * </pre>
     *
     * @param n maximum elements to retrieve
     * @return (up to) the first n elements of the queue
     * @throws InterruptedException if thread was interrupted
     */
    public List<Trace> take(int n) throws InterruptedException {
        List<Trace> traces = new ArrayList<>(Math.min(n, queue.size()));

        Trace firstTrace = queue.take();
        traces.add(firstTrace);

        queue.drainTo(traces, n - 1);

        log.info("Retrieved {} traceData from cache", traces.size());
        return traces;
    }

    public void addSeed(Seed seed) {
        queue.clear();
        List<Trace> seededTraces = new ArrayList<>(seed.nrTraces());
        List<Span> spans = new ArrayList<>(seed.traceData().getSpans().size());
        seed.traceData().getSpans().forEach(span -> {
            spans.add(Span.builder().json(span).build());
        });
        Trace t;
        for (int i = 0; i < seed.nrTraces(); i++) {
            t = Trace.builder()
                    .id(seed.traceData().getTraceId() + i)
                    .traceDataType(seed.traceData().getTraceDataType())
                    .nrNodes(seed.traceData().getNrNodes())
                    .spans(List.copyOf(spans))
                    .build();
            seededTraces.add(t);
        }
        queue.addAll(seededTraces);
        isDead.set(false);
        isSeeded.set(true);
    }

    public boolean isCacheDone() {
        return isDead.getAcquire()
               || sourceDetails != null && sourceDetails.getPage() == sourceDetails.getTotalPages() && queue.isEmpty()
               || isSeeded.getAcquire() && queue.isEmpty();
    }

    private void cache() {
        log.info("trying to cache next page");
        if (!isDead.getAcquire() && !isSeeded.getAcquire()) {
            Page<Trace> page = retrieveNextPage();
            log.info("caching next page");
            if (!isDead.getAcquire() && !isSeeded.getAcquire()) {
                queue.addAll(page.getContent());
                sourceDetails.setTotalPages(page.getTotalPages());
                sourceDetails.setPage(Math.min(page.getNumber() + 1, page.getTotalPages()));
                if (page.getNumber() < page.getTotalPages() && !isPaused.get()) {
                    log.info("page not last and cache not paused --> submitting next cache cycle");
                    executor.submit(this::cache);
                } else {
                    log.info("page was last or cache is paused --> setting out next cycle");
                    log.info("(page:{}/{}) (paused: {})", sourceDetails.getPage(), sourceDetails.getTotalPages(),
                            isPaused.get());
                }
            }
        } else {
            log.info("cache is stopped");
        }
    }

    private Page<Trace> retrieveNextPage() {
        if (sourceDetails == null) {
            throw new TraceCacheException("Source details not initialized");
        }
        log.info("Retrieving next({}) page", sourceDetails.getPage()+1);
        ResponseEntity<RestPageImpl<Trace>> response = restTemplate.exchange(
                restConfig.tracesUrl
                + "?sourceFile=" + sourceDetails.getSourceFile()
                + "&page=" + sourceDetails.getPage()
                + "&size=" + sourceDetails.getSize()
                + "&sort=" + sourceDetails.getSort(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Page<Trace> page = Objects.requireNonNull(response.getBody());
            log.info("retrieval successful ({}/{})", page.getNumber() + 1, page.getTotalPages());
            return page;
        }
        String errorMessage = String.format(
                "error retrieving next page (%d of %d)",
                sourceDetails.getPage(),
                sourceDetails.getTotalPages()
        );

        log.error(errorMessage);
        throw new TraceCacheException(errorMessage);
    }
}
