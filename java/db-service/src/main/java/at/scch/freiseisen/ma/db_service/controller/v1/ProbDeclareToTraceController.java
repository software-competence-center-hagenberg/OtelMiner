package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareService;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareToTraceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        List<ProbDeclareToTrace> toBePersisted = new ArrayList<>();
        List<ProbDeclareToTraceId> ids = new ArrayList<>();
        traces.forEach(t -> {
            ProbDeclareToTrace pdt = new ProbDeclareToTrace(probDeclare, t);
            toBePersisted.add(pdt);
            ids.add(new ProbDeclareToTraceId(pdt.getProbDeclareId(), pdt.getTraceId()));
        });
        List<ProbDeclareToTrace> toBeChecked = service.findAllById(ids);
        toBeChecked.forEach(pdt -> {
            log.error("ProbDeclareToTrace entity with with probDeclareId {} and traceId {}, already exists!",
                    pdt.getProbDeclareId(),
                    pdt.getTraceId());
            log.error("not persisting...");
            toBePersisted.remove(pdt);
        });
        service.saveAll(toBePersisted);
    }

    @GetMapping("/{prob-declare-id}/nr-traces")
    public long retrieveNrTraces(@PathVariable("prob-declare-id") String probDeclareId) {
        return service.countTraces(probDeclareId);
    }
}
