package at.scch.freiseisen.ma.db_service.controller;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import at.scch.freiseisen.ma.data_layer.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseController<
        S extends BaseService<R, E, T>,
        R extends BaseRepository<E, T>,
        E extends BaseEntity<T>,
        T extends Serializable> {
    protected final S service;

    public abstract Page<E> retrieveAll(@Param("page") int page, @Param("size") int size, @Param("sort") String sort);

    public abstract E retrieveOne(@PathVariable("id") String id);

    public abstract E postOne(@RequestBody E entity);

    public abstract List<E> post(@RequestBody List<E> entities);

    public abstract void deleteOne(@PathVariable("id") T id);

    public abstract void deleteAllByIdInBatch(@RequestBody T[] ids);
}
