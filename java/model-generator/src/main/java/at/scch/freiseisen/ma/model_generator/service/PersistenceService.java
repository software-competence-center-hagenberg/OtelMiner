package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistenceService {
    private final RestConfig restConfig;
    private final RestTemplate restTemplate;

    void createAndPersistProbDeclareToTrace(String probDeclareId, List<Trace> traces) {
        log.debug("creating association between prob declare model {} and trace ids: {}",
                probDeclareId, traces.stream().map(Trace::getId).collect(Collectors.joining(", ")));
        restTemplate.postForLocation(restConfig.probDeclareToTraceUrl + "/" + probDeclareId, traces);
    }

    ProbDeclare persistProbDeclare(ProbDeclare probDeclare) {
        return restTemplate.postForObject(restConfig.probDeclareUrl + "/one", probDeclare, ProbDeclare.class);
    }

    void stopProbDeclareGeneration(String probDeclareId) {
        restTemplate.delete(restConfig.probDeclareUrl + "/stop-generation/" + probDeclareId); // FIXME change to POST
    }

    void persistConstraints(Collection<ProbDeclareConstraintModelEntry> values, String probDeclareId) {
        restTemplate.postForLocation(restConfig.declareUrl + "/" + probDeclareId, values);
    }

    public ProbDeclareModel getProbDeclareModel(String id) {
        return restTemplate.getForObject(restConfig.probDeclareUrl + "/model/" + id, ProbDeclareModel.class);
    }

    public long retrieveNumberTracesForProbDeclare(String probDeclareId) {
        return restTemplate.getForObject(restConfig.probDeclareToTraceUrl + "/" + probDeclareId + "/nr-traces", Long.class );
    }
}
