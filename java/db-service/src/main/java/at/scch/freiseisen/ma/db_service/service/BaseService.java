package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseService<R extends BaseRepository<E, T>, E extends BaseEntity<T>, T extends Serializable> {

    protected final R repository;

    public Page<E> findAll(int page, int size, Sort sort) {
        log.info("retrieving page {} of size {} with sort {}", page, size, sort);
        return repository.findAll(PageRequest.of(page, size, sort));
    }

    public void save(E entity) {
        log.info("persisting entity {}", entity.getId());
        repository.saveAndFlush(entity);
    }

    public void saveAll(List<E> entities) {
        log.info("persisting entities {}", entities.stream().map(BaseEntity::getId).toList());
        repository.saveAllAndFlush(entities);
    }

    public void delete(T id) {
        log.info("deleting entry with id {}", id);
        repository.delete(repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Entity with id " + id + " not found!")));
    }

    public void deleteAllByIdInBatch(List<T> ids) {
        log.info("deleting entries with ids in batch {}", ids);
        repository.deleteAllByIdInBatch(ids);
    }

    public E findById(T id) {
        log.info("retrieving entry with id {}", id);
        return repository.findById(id).orElse(null);
    }
}

