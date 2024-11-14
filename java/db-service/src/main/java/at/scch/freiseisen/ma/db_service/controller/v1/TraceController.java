package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.otel.TraceRepository;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import at.scch.freiseisen.ma.db_service.service.TraceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/traces")
public class TraceController extends BaseController<TraceService, TraceRepository, Trace, String> {

    public TraceController(TraceService service) {
        super(service);
    }

    @PostMapping("/source-details")
    public SourceDetails retrieveSourceDetails(@RequestBody SourceDetails sourceDetails) {
        return service.findSourceDetails(sourceDetails);
    }

    @GetMapping("/data-overview")
    public List<DataOverview> retrieveDataOverview() {
        return service.findDataOverview();
    }

    @Override
    @GetMapping
    public Page<Trace> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @GetMapping("/{id}/spans")
    public List<String> retrieveSpansAsStringList(@PathVariable("id") String id) {
        return service.findById(id).getSpans().stream().map(Span::getJson).toList();
    }

    @Override
    @GetMapping("/{id}")
    public Trace retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @Override
    @PostMapping("/one")
    public void postOne(@RequestBody Trace entity) {
        service.save(entity);
    }

    @Override
    @PostMapping
    public void post(@RequestBody List<Trace> entities) {
        service.saveAll(entities);
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
