package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.commons.RestPageImpl;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import at.scch.freiseisen.ma.model_generator.error.TraceCacheException;
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
import java.util.stream.Collectors;

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
        return queue.take();
    }

    /**
     * Retrieves up to n elements from the queue. The first element is retrieved with a blocking call,
     * the rest non-blocking.
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

        log.info("Retrieved {} traces from cache", traces.size());
        return traces;
    }

    public boolean isCacheDone() {
        return isDead.getAcquire()
               || sourceDetails != null && sourceDetails.getPage() == sourceDetails.getTotalPages() && queue.isEmpty();
    }

    private void cache() {
        log.info("trying to cache next page");
        if (!isDead.getAcquire()) {
            Page<Trace> page = retrieveNextPage();
            log.info("caching next page");
            if (!isDead.getAcquire()) {
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
            persistenceService.createAndPersistProbDeclareToTrace(probDeclareId, page.getContent());
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
