package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareService;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareToTraceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("v1/prob-declare-to-trace")
public class ProbDeclareToTraceController {
    private final ProbDeclareToTraceService service;
    private final ProbDeclareService probDeclareService;

    @GetMapping("/{id}")
    public ProbDeclareToTrace retrieveOne(@PathVariable("id") ProbDeclareToTraceId id) {
        return service.findById(id);
    }

    @PostMapping("/one")
    public void postOne(@RequestBody ProbDeclareToTrace entity) {
        service.save(entity);
    }

    @PostMapping("/{pb-id}")
    public void post(@PathVariable("pb-id") String probDeclareId, @RequestBody List<Trace> traces) {
        ProbDeclare probDeclare = probDeclareService.findById(probDeclareId);
        List<ProbDeclareToTrace> entities = traces.stream()
                .map(t -> new ProbDeclareToTrace(probDeclare, t))
                .toList();
        service.saveAll(entities);
    }
}
