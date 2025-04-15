package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.trace_collector.configuration.OtelToProbdeclareConfiguration;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclareService {
    private final RestTemplate restTemplate;
    private final RestConfig restConfig;
    private final RabbitTemplate rabbitTemplate;
    private final OtelToProbdeclareConfiguration otelToProbdeclareConfiguration;
    private final Map<String, String[]> declarePerTrace = new ConcurrentHashMap<>();

    public String generateFromSpanList(String traceId, List<String> spans) {
        log.info("generating declare model for trace...");// {}... deleting existing", traceId);
//        restTemplate.delete(restConfig.declareUrl + "/" + traceId); TODO delete assoc 
        declarePerTrace.put(traceId, new String[]{});
        transformAndPipe(traceId, spans, TraceDataType.JAEGER_SPANS_LIST);
        return traceId;
    }

    void transformAndPipe(String traceId, List<String> spans, TraceDataType traceDataType) {
        String routingKey = otelToProbdeclareConfiguration.determineRoutingKey(traceDataType);
        rabbitTemplate.convertAndSend(routingKey, "{ traceId: \"" + traceId + "\", spans: [" + String.join(",", spans) + "] }");
    }

    void add(String traceId, String[] declare) {
        declarePerTrace.put(traceId, declare);
    }

    public String[] retrieve(String traceId) {
        log.info("retrieving declare model for trace: {}", traceId);
        if (declarePerTrace.containsKey(traceId)) {
            String[] declare = declarePerTrace.get(traceId);
            log.info("retrieved declare: {}", Arrays.toString(declare));
            return declare;
        }
        log.info("no model present in local storage -> accessing data base");
        String[] declare = Objects.requireNonNull(
                restTemplate.getForEntity(
                        restConfig.declareUrl + "/" + traceId,
                        String[].class
                ).getBody()
        );
        declarePerTrace.put(traceId, declare);
        return declare;
    }

    public void clear() {
        declarePerTrace.clear();
    }
}
