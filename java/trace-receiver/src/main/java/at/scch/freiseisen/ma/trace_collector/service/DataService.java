package at.scch.freiseisen.ma.trace_collector.service;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.trace_collector.configuration.RestConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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

    public List<DataOverview> getDataOverview() {
        return Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(restConfig.dataOverviewUrl, DataOverview[].class)));
    }

    public Page<SourceDetails> getDetails(String sourceFile) {
    }
}
