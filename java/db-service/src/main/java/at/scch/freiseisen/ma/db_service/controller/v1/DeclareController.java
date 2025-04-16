package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.DeclareId;
import at.scch.freiseisen.ma.data_layer.service.DeclareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/declare")
public class DeclareController {
    private final DeclareService service;

    @GetMapping
    public Page<Declare> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @GetMapping("/{prob-declare-id}/{constraint}")
    public Declare retrieveOne(@PathVariable("prob-declare-id") String probDeclareId, @PathVariable("constraint") String constraint) {
        return service.findById(new DeclareId(probDeclareId, constraint));
    }

    @PostMapping("/one")
    public Declare postOne(@RequestBody Declare entity) {
        return service.save(entity);
    }

    @PostMapping
    public List<Declare> post(@RequestBody List<Declare> entities) {
        return service.saveAll(entities); // TODO obsolete??
    }

    @PostMapping("/{probDeclareId}")
    public List<Declare> post(@PathVariable String probDeclareId, @RequestBody List<ProbDeclareConstraintModelEntry> modelEntries) {
        return service.update(modelEntries, probDeclareId);
    }

    @DeleteMapping("/{prob-declare-id}/{constraint}")
    public void deleteOne(@PathVariable("prob-declare-id") String probDeclareId, @PathVariable("constraint") String constraint) {
        service.delete(new DeclareId(probDeclareId, constraint));
    }

    @DeleteMapping("/{prob-declare-id}")
    public void deleteAllByIdInBatch(@PathVariable("prob-declare-id") String probDeclareId, @RequestBody String[] constraints) {
        service.deleteAllByIdInBatch(Arrays.stream(constraints).map(c -> new DeclareId(probDeclareId, c)).toList());
    }

    @PostMapping("/by-constraint-template/{prob-declare-id}")
    public List<ProbDeclareConstraintModelEntry> postByConstraintTemplate(
            @RequestBody List<String> constraintTemplates,
            @PathVariable("prob-declare-id") String probDeclareId
    ) {
        return service.findAllByConstraintTemplateInAndProbDeclare(constraintTemplates, probDeclareId);
    }

    @PostMapping("/add-constraints/{prob-declare-id}")
    public List<ProbDeclareConstraintModelEntry> postAddConstraints(
            @RequestBody ConversionResponse conversionResponse,
            @PathVariable("prob-declare-id") String probDeclareId
    ){
        return service.addNewlyConverted(conversionResponse, probDeclareId);
    }
}
