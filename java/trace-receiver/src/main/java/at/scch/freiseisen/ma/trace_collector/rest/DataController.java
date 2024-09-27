package at.scch.freiseisen.ma.trace_collector.rest;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.trace_collector.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data")
@CrossOrigin("http://localhost:3000")
public class DataController {
    private final DataService dataService;

    @GetMapping("/overview")
    public List<DataOverview> getDataOverview() {
        return dataService.getDataOverview();
    }

    @PostMapping("/details")
    public SourceDetails getDataOverview(@RequestBody  SourceDetails sourceDetails) {
        return dataService.getDetails(sourceDetails);
    }

    @PostMapping("/generate-model")
    public String generateModel(@RequestBody TraceData traceDetails) {
        return "TODO: implement";
    }
}
