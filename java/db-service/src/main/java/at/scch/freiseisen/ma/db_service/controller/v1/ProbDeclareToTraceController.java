package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import at.scch.freiseisen.ma.db_service.service.ProbDeclareToTraceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("v1/prob-declare-to-trace")
public class ProbDeclareToTraceController {
    private final ProbDeclareToTraceService service;

    @GetMapping("/{id}")
    public ProbDeclareToTrace retrieveOne(@PathVariable("id") ProbDeclareToTraceId id) {
        return service.findById(id);
    }

    @PostMapping("/one")
    public void postOne(@RequestBody ProbDeclareToTrace entity) {
        service.save(entity);
    }

    @PostMapping
    public void post(@RequestBody List<ProbDeclareToTrace> entities) {
        service.saveAll(entities);
    }
}
