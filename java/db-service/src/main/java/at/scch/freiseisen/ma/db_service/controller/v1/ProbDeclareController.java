package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraint;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareInfo;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.ProbDeclareRepository;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareService;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareToTraceService;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("v1/prob-declare")
public class ProbDeclareController extends BaseController<ProbDeclareService, ProbDeclareRepository, ProbDeclare, String> {
    private final ProbDeclareToTraceService probDeclareToTraceService;

    public ProbDeclareController(ProbDeclareService service, ProbDeclareToTraceService probDeclareToTraceService) {
        super(service);
        this.probDeclareToTraceService = probDeclareToTraceService;
    }

    @Override
    @GetMapping
    public Page<ProbDeclare> retrieveAll(@RequestParam("page") int page, @RequestParam("size") int size,
                                         @RequestParam("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @GetMapping("/existing")
    public ProbDeclareInfo[] retrieveAllBySourceFile(@RequestParam("source-file")  String sourceFile) {
        List<ProbDeclare> entities = probDeclareToTraceService.findDistinctProbDeclareIdByTraceSourceFile(sourceFile);
        return entities.stream()
                .map(e -> new ProbDeclareInfo(e.getId(), e.getInsertDate(), e.getUpdateDate(), e.isGenerating()))
                .toArray(ProbDeclareInfo[]::new);
    }

    @Override
    @GetMapping("/{id}")
    public ProbDeclare retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @GetMapping("/model/{id}")
    public ProbDeclareModel retrieveModel(@PathVariable("id") String id) {
        ProbDeclare probDeclare = service.findById(id);
        List<ProbDeclareConstraint> constraints = probDeclare.getDeclareList().stream()
                .map(d -> new ProbDeclareConstraint(d.getProbability(), d.getConstraintTemplate(), d.getNr()))
                .toList();
        return new ProbDeclareModel(probDeclare.getId(), constraints, probDeclare.isGenerating(), false);
    }

    @DeleteMapping("/stop-generation/{id}")
    public void stopGeneration(@PathVariable("id") String id) {
        ProbDeclare probDeclare = service.findById(id);
        if (!probDeclare.isGenerating()) {
            return;
        }
        probDeclare.setGenerating(false);
        probDeclare.setUpdateDate(LocalDateTime.now());
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
