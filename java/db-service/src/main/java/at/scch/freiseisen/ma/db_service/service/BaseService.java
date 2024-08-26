package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseService<R extends BaseRepository<E, T>, E extends BaseEntity<T>, T extends Serializable> {

    protected final R repository;

    public Page<E> findAll(int page, int size, Sort sort) {
        return repository.findAll(PageRequest.of(page, size, sort));
    }

    public void save(E entity) {
        repository.saveAndFlush(entity);
    }

    public void saveAll(List<E> entities) {
        repository.saveAllAndFlush(entities);
    }

    public void delete(T id) {
        repository.delete(repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found!")));
    }

    public void deleteAllByIdInBatch(List<T> ids) {
        repository.deleteAllByIdInBatch(ids);
    }

    public E findById(T id) {
        return repository.findById(id).orElse(null);
    }
}

