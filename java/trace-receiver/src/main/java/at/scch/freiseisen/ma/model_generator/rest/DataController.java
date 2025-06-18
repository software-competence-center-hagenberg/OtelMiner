package at.scch.freiseisen.ma.model_generator.rest;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.model_generator.service.CanonizedSpanTreeService;
import at.scch.freiseisen.ma.model_generator.service.DataService;
import at.scch.freiseisen.ma.model_generator.service.DeclareService;
import at.scch.freiseisen.ma.model_generator.service.ProbDeclareManagerService;
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
    public ProbDeclareModel generateProbDeclareModel(@RequestParam("expected-traces") int expectedTraces, @RequestBody SourceDetails sourceDetails) {
        return dataService.generateProbDeclareModel(sourceDetails, expectedTraces);
    }

    @GetMapping("/prob-declare/abort/{prob-declare-id}")
    public ResponseEntity<Void> abortProbDeclareModelGeneration(@PathVariable("prob-declare-id") String probDeclareId) {
        log.info("aborting generation of model {}", probDeclareId);
        return dataService.abortProbDeclareModelGeneration()
                ? ResponseEntity.ok().build()
                : ResponseEntity.internalServerError().build();
    }

    @GetMapping("/prob-declare/pause/{prob-declare-id}")
    public ResponseEntity<Void> pauseProbDeclareModelGeneration(@PathVariable("prob-declare-id") String probDeclareId) {
        log.info("pausing generation of model {}", probDeclareId);
        return dataService.pauseProbDeclareModelGeneration(probDeclareId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.internalServerError().build();
    }

    @GetMapping("/prob-declare/resume/{prob-declare-id}")
    public ResponseEntity<Void> resumeProbDeclareModelGeneration(@PathVariable("prob-declare-id") String probDeclareId) {
        log.info("resuming generation of model {}", probDeclareId);
        return dataService.resumeProbDeclareModelGeneration(probDeclareId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.internalServerError().build();
    }

    @GetMapping("/prob-declare/{id}")
    public ProbDeclareModel getProbDeclareModel(@PathVariable("id") String id) {
        return dataService.getProbDeclareModel(id);
    }

    @PostMapping("/declare/generate")
    public String generateDeclareModelForTrace(@RequestBody TraceData traceData) {
        return declareService.generateFromSpanList(
                traceData.getTraceId(), traceData.getSpans(), determineTraceDataType(traceData));
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
        return canonizedSpanTreeService.generateSpanTreesFromSpanList(
                traceData.getTraceId(), traceData.getSpans(), determineTraceDataType(traceData));
    }

    private TraceDataType determineTraceDataType(TraceData traceData) {
        return  traceData.getTraceDataType() == null
                ? TraceDataType.JAEGER_SPANS_LIST
                : TraceDataType.valueOf(traceData.getTraceDataType());
    }
}
