package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseService<R extends BaseRepository<E, T>, E extends BaseEntity<T>, T extends Serializable> {

    protected final R repository;

    public Page<E> findAll(int page, int size, Sort sort) {
        log.debug("retrieving page {} of size {} with sort {}", page, size, sort);
        return repository.findAll(PageRequest.of(page, size, sort));
    }

    public E save(E entity) {
        log.debug("persisting entity {}", entity.getId());
        return repository.saveAndFlush(entity);
    }

    public List<E> saveAll(List<E> entities) {
        log.debug("persisting {} entities", entities.size());
        return repository.saveAllAndFlush(entities);
    }

    public void delete(T id) {
        log.debug("deleting entry with id {}", id);
        Optional<E> entity = repository.findById(id);
        if (entity.isPresent()) {
            repository.delete(entity.get());
        } else {
            log.error("entity not found");
        }
    }

    public void deleteAllByIdInBatch(List<T> ids) {
        log.debug("deleting entries with ids in batch {}", ids);
        repository.deleteAllByIdInBatch(ids);
    }

    public E findById(T id) {
        log.debug("retrieving entity with id {}", id);
        return repository.findById(id).orElse(null);
    }

    public E safeFindById(T id) {
        log.debug("safe retrieving entity with id {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity with id " + id + " not found"));
    }
}

