package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.ProbDeclareToTraceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProbDeclareToTraceService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ProbDeclareToTraceRepository repository;

    public void save(ProbDeclareToTrace entity) {
        log.info("persisting entity prob declare id: {}, trace id: {}",
                entity.getProbDeclare().getId(),
                entity.getTrace().getId());
        repository.saveAndFlush(entity);
    }

    public void saveAll(List<ProbDeclareToTrace> entities) {
        log.info("persisting {} entities", entities.size());
        repository.saveAllAndFlush(entities);
    }

    public ProbDeclareToTrace findById(ProbDeclareToTraceId id) {
        log.info("retrieving entry with id {}", id);
        return repository.findById(id).orElse(null);
    }

    public ProbDeclareToTrace safeFindById(ProbDeclareToTraceId id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity with id " + id + " not found"));
    }

    public List<ProbDeclare> findDistinctProbDeclareIdByTraceSourceFile(String sourceFile) {
        return repository.findDistinctProbDeclareByTraceSourceFile(sourceFile);
    }
}

