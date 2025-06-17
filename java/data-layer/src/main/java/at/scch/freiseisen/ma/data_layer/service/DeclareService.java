package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.*;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.DeclareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclareService {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final DeclareRepository repository;
    private final ProbDeclareService probDeclareService;
    private final ProbDeclareToTraceService probDeclareToTraceService;

    public List<ProbDeclareConstraintModelEntry> findAllByConstraintTemplateInAndProbDeclare(List<String> constraintTemplates, String probDeclareId) {
        ProbDeclare probDeclare = probDeclareService.safeFindById(probDeclareId);
        assert probDeclare != null;
        return mapToModel(repository.findAllById(constraintTemplates.stream().map(c -> new DeclareId(probDeclareId, c)).toList()));
    }

    private List<ProbDeclareConstraintModelEntry> mapToModel(List<Declare> entities) {
        return entities.stream()
                .map(declare ->
                        new ProbDeclareConstraintModelEntry(
                                declare.getConstraintTemplate(),
                                declare.getProbability(),
                                declare.getNr())
                ).toList();
    }

//    public List<ProbDeclareConstraintModelEntry> addNewlyConverted(ConversionResponse response, String probDeclareId) {
//        log.info("retrieving prob declare with id {}", probDeclareId);
//
//        ProbDeclareToTraceId probDeclareToTraceId = new ProbDeclareToTraceId(probDeclareId, response.traceId());
//        ProbDeclareToTrace probDeclareToTrace = probDeclareToTraceService.safeFindById(probDeclareToTraceId);
//
//        List<Declare> entities = Arrays.stream(response.constraints())
//                .map(c -> new Declare(probDeclareToTrace, c))
//                .toList();
//
//        return mapToModel(saveAll(entities));
//    }

    public List<Declare> update(List<ProbDeclareConstraintModelEntry> modelEntries, String probDeclareId) {
        Map<DeclareId, ProbDeclareConstraintModelEntry> map = modelEntries.stream()
                .collect(Collectors.toMap(pme -> new DeclareId(probDeclareId, pme.getConstraintTemplate()), Function.identity()));
        log.info("received {} model entries for prob declare {}", modelEntries.size(), probDeclareId);
        List<Declare> entities = repository.findAllById(map.keySet());
        log.info("{} declare entries already exist for prob declare {}", entities.size(), probDeclareId);
        entities.forEach(e -> {
            ProbDeclareConstraintModelEntry modelEntry = map.remove(new DeclareId(probDeclareId, e.getConstraintTemplate()));
            e.setNr(modelEntry.getNr());
            e.setProbability(modelEntry.getProbability());
            e.setUpdateDate(LocalDateTime.now());
        });
        List<Declare> newEntitities = new ArrayList<>();
        ProbDeclare probDeclare = probDeclareService.safeFindById(probDeclareId);
        map.values().forEach(e -> newEntitities.add(new Declare(e, probDeclare)));
        log.info("saving {} new model entries for prob declare {}", newEntitities.size(), probDeclareId);
        saveAll(newEntitities);
        log.info("updating {} existing entities for prob declare {}", entities.size(), probDeclareId);
        return saveAll(entities);
    }

    public List<Declare> saveAll(List<Declare> entities) {
        log.info("persisting {} entities", entities.size());
        return repository.saveAllAndFlush(entities);
    }

    public void delete(DeclareId id) {
        repository.deleteById(id);
    }

    public void deleteAllByIdInBatch(List<DeclareId> ids) {
        repository.deleteAllByIdInBatch(ids);
    }

    public Page<Declare> findAll(int page, int size, Sort by) {
        log.debug("retrieving page {} of size {} with sort {}", page, size, by);
        return repository.findAll(PageRequest.of(page, size, by));
    }

    public Declare findById(DeclareId id) {
        log.debug("retrieving entity with id {}", id);
        return repository.findById(id).orElse(null);
    }

    public Declare save(Declare entity) {
        log.debug("persisting entity {}-{}", entity.getProbDeclare().getId(), entity.getConstraintTemplate());
        return repository.saveAndFlush(entity);
    }
}
