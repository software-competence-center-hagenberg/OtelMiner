package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.dto.DeclareConstraint;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.ProbDeclareRepository;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareService;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/prob-declare")
public class ProbDeclareController extends BaseController<ProbDeclareService, ProbDeclareRepository, ProbDeclare, String> {
    public ProbDeclareController(ProbDeclareService service) {
        super(service);
    }

    @Override
    @GetMapping
    public Page<ProbDeclare> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @Override
    @GetMapping("/{id}")
    public ProbDeclare retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @GetMapping("/model/{id}")
    public ProbDeclareModel retrieveModel(@PathVariable("id") String id) {
        ProbDeclare probDeclare = service.findById(id);
        List<DeclareConstraint> constraints = probDeclare.getDeclareList().stream()
                .map(d -> new DeclareConstraint(d.getProbability(), d.getConstraintTemplate()))
                .toList();
        return new ProbDeclareModel(probDeclare.getId(), constraints, probDeclare.isGenerating());
    }

    @DeleteMapping("/stop-generation/{id}")
    public void stopGeneration(@PathVariable("id") String id) {
        ProbDeclare probDeclare = service.findById(id);
        probDeclare.setGenerating(false);
        service.save(probDeclare);
    }

    @Override
    @PostMapping("/one")
    public ProbDeclare postOne(@RequestBody ProbDeclare entity) {
        return service.save(entity);
    }

    @Override
    @PostMapping
    public List<ProbDeclare> post(@RequestBody List<ProbDeclare> entities) {
        return service.saveAll(entities);
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable("id") String id) {
        service.delete(id);
    }

    @Override
    @DeleteMapping
    public void deleteAllByIdInBatch(@RequestBody String[] ids) {
        service.deleteAllByIdInBatch(List.of(ids));
    }
}
