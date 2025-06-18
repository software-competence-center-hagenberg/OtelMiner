package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {
    private final RestConfig restConfig;
    private final RestTemplate restTemplate;
    private final CanonizedSpanTreeService canonizedSpanTreeService;
    private final ProbDeclareManagerService probDeclareManagerService;

    public List<DataOverview> getDataOverview() {
        return Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(restConfig.dataOverviewUrl, DataOverview[].class)));
    }

    public SourceDetails getDetails(SourceDetails sourceDetails) {
        HttpEntity<SourceDetails> requestEntity = new HttpEntity<>(sourceDetails);
        ResponseEntity<SourceDetails> response = restTemplate.exchange(
                restConfig.sourceDetailsUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        return Objects.requireNonNull(response.getBody());
    }

    // FIXME change again so only source file is received in request and manage rest with pages in manager service!
    public ProbDeclareModel generateProbDeclareModel(SourceDetails sourceDetails, int expectedTraces) {
        return probDeclareManagerService.generate(sourceDetails, expectedTraces);
    }

    public ProbDeclareModel getProbDeclareModel(String id) {
        return probDeclareManagerService.getProbDeclareModel(id);
    }

    public boolean abortProbDeclareModelGeneration() {
        return probDeclareManagerService.abort();
    }

    public boolean pauseProbDeclareModelGeneration(String probDeclareId) {
        return probDeclareManagerService.pause(probDeclareId);
    }

    public boolean resumeProbDeclareModelGeneration(String probDeclareId) {
        return probDeclareManagerService.resume(probDeclareId);
    }
}
