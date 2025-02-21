package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.repository.otel.SpanRepository;
import at.scch.freiseisen.ma.data_layer.service.SpanService;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/spans")
public class SpanController extends BaseController<SpanService, SpanRepository, Span, String> {
    public SpanController(SpanService service) {
        super(service);
    }

    @Override
    @GetMapping
    public Page<Span> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @Override
    @GetMapping("/{id}")
    public Span retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @Override
    @PostMapping("/one")
    public Span postOne(@RequestBody Span entity) {
        return service.save(entity);
    }

    @Override
    @PostMapping
    public List<Span> post(@RequestBody List<Span> entities) {
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
