package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.pre_processing.CanonizedSpanTree;
import at.scch.freiseisen.ma.data_layer.repository.pre_processing.CanonizedSpanTreeRepository;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import at.scch.freiseisen.ma.db_service.service.CanonizedSpanTreeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/canonized-span-tree")
public class CanonizedSpanTreeController extends BaseController<CanonizedSpanTreeService, CanonizedSpanTreeRepository, CanonizedSpanTree, String> {

    public CanonizedSpanTreeController(CanonizedSpanTreeService service) {
        super(service);
    }

    @Override
    @GetMapping
    public Page<CanonizedSpanTree> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @Override
    @GetMapping("/{id}")
    public CanonizedSpanTree retrieveOne(@PathVariable("id") String id) {
        return service.findById(id);
    }

    @Override
    @PostMapping("/one")
    public CanonizedSpanTree postOne(@RequestBody CanonizedSpanTree entity) {
        return service.save(entity);
    }

    @Override
    @PostMapping
    public List<CanonizedSpanTree> post(@RequestBody List<CanonizedSpanTree> entities) {
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
