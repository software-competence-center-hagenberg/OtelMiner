package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
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

    public String generateProbDeclareModel(SourceDetails sourceDetails) {
        return probDeclareManagerService.generate(sourceDetails);
    }

    public ProbDeclareModel getProbDeclareModel(String id) {
        return probDeclareManagerService.getProbDeclareModel(id);
    }
}
