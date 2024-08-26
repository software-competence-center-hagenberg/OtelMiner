package at.scch.freiseisen.ma.db_service.controller;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import at.scch.freiseisen.ma.db_service.service.BaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseController<
        S extends BaseService<R, E, T>,
        R extends BaseRepository<E, T>,
        E extends BaseEntity<T>,
        T extends Serializable> {
    protected final S service;

    @GetMapping
    public Page<E> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort) {
        return service.findAll(page, size, Sort.by(sort));
    }

    @GetMapping("/{id}")
    public E retrieveOne(@PathVariable("id") T id) {
        return service.findById(id);
    }

    @PostMapping("/one")
    public void postOne(E entity) {
        service.save(entity);
    }

    @PostMapping
    public void post(List<E> entities) {
        service.saveAll(entities);
    }

    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable("id") T id) {
        service.delete(id);
    }

    @DeleteMapping
    public void deleteAllByIdInBatch(List<T> ids) {
        service.deleteAllByIdInBatch(ids);
    }
}
