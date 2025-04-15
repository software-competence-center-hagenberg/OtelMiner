package at.scch.freiseisen.ma.trace_collector.rest;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.trace_collector.service.CanonizedSpanTreeService;
import at.scch.freiseisen.ma.trace_collector.service.DataService;
import at.scch.freiseisen.ma.trace_collector.service.DeclareService;
import at.scch.freiseisen.ma.trace_collector.service.ProbDeclareManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data")
@CrossOrigin({"http://localhost:3000", "http://frontend:3000"})
public class DataController {
    private final DataService dataService;
    private final ProbDeclareManagerService probDeclareManagerService;
    private final CanonizedSpanTreeService canonizedSpanTreeService;
    private final DeclareService declareService;

    @GetMapping("/overview")
    public List<DataOverview> getDataOverview() {
        return dataService.getDataOverview();
    }

    @PostMapping("/details")
    public SourceDetails getDataOverview(@RequestBody  SourceDetails sourceDetails) {
        return dataService.getDetails(sourceDetails);
    }

    @PostMapping("/prob-declare/generate")
    public ProbDeclareModel generateProbDeclareModel(@RequestBody SourceDetails sourceDetails) {
        return dataService.generateProbDeclareModel(sourceDetails);
    }

    @PostMapping("/prob-declare/abort")
    public ResponseEntity<Void> abortProbDeclareModelGeneration() {
        return dataService.abortProbDeclareModelGeneration()
                ? ResponseEntity.ok().build()
                : ResponseEntity.internalServerError().build();
    }

    @GetMapping("/prob-declare/{id}")
    public ProbDeclareModel getProbDeclareModel(@PathVariable("id") String id) {
        return dataService.getProbDeclareModel(id);
    }

    @PostMapping("/declare/generate")
    public String generateDeclareModelForTrace(@RequestBody TraceData traceData) {
        return declareService.generateFromSpanList(traceData.getTraceId(), traceData.getSpans());
    }

    @GetMapping("/declare/{id}")
    public String[] checkDeclareModelForTrace(@PathVariable("id") String traceId) {
        return declareService.retrieve(traceId);
    }

    @GetMapping("/span-trees/{id}")
    public String checkSpanTreesForTrace(@PathVariable("id") String traceId) {
        return canonizedSpanTreeService.retrieveSpanTrees(traceId);
    }

    @PostMapping("/span-trees/generate")
    public String generateSpanTrees(@RequestBody TraceData traceData) {
        return canonizedSpanTreeService.generateSpanTreesFromSpanList(traceData.getTraceId(), traceData.getSpans());
    }
}
