package at.scch.freiseisen.ma.trace_collector.rest;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.trace_collector.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/details")
    public List<DataOverview> getDataOverview(@RequestParam String sourceFile) {
        return dataService.getDetails(sourceFile);
    }
}
