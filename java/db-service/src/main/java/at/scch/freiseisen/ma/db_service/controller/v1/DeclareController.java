package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.DeclareRepository;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import at.scch.freiseisen.ma.db_service.service.DeclareService;
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

    @GetMapping
    public Page<Declare> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @GetMapping("/{id}")
    public Declare retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @PostMapping("/one")
    public void postOne(@RequestBody Declare entity) {
        service.save(entity);
    }

    @PostMapping
    public void post(@RequestBody List<Declare> entities) {
        service.saveAll(entities);
    }

    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable("id") String id) {
        service.delete(id);
    }

    @DeleteMapping
    public void deleteAllByIdInBatch(@RequestBody String[] ids) {
        service.deleteAllByIdInBatch(List.of(ids));
    }
}
