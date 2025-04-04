package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.DeclareRepository;
import at.scch.freiseisen.ma.data_layer.service.DeclareService;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/declare")
public class DeclareController extends BaseController<DeclareService, DeclareRepository, Declare, String> {

    public DeclareController(DeclareService service) {
        super(service);
    }

    @Override
    @GetMapping
    public Page<Declare> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @Override
    @GetMapping("/{id}")
    public Declare retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @Override
    @PostMapping("/one")
    public Declare postOne(@RequestBody Declare entity) {
        return service.save(entity);
    }

    @Override
    @PostMapping
    public List<Declare> post(@RequestBody List<Declare> entities) {
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
