package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.dto.ConversionResponse;
import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.DeclareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DeclareService extends BaseService<DeclareRepository, Declare, String> {
    private final ProbDeclareService probDeclareService;
    private final ProbDeclareToTraceService probDeclareToTraceService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DeclareService(
            DeclareRepository repository,
            ProbDeclareService probDeclareService,
            ProbDeclareToTraceService probDeclareToTraceService) {
        super(repository);
        this.probDeclareService = probDeclareService;
        this.probDeclareToTraceService = probDeclareToTraceService;
    }

    public List<ProbDeclareConstraintModelEntry> findAllByConstraintTemplateInAndProbDeclare(List<String> constraintTemplates, String probDeclareId) {
        ProbDeclare probDeclare = probDeclareService.safeFindById(probDeclareId);
        return mapToModel(repository.findAllByConstraintTemplateInAndProbDeclare(constraintTemplates, probDeclare));
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

    public List<ProbDeclareConstraintModelEntry> addNewlyConverted(ConversionResponse response, String probDeclareId) {
        log.info("retrieving prob declare with id {}", probDeclareId);

        ProbDeclareToTraceId probDeclareToTraceId = new ProbDeclareToTraceId(probDeclareId, response.traceId());
        ProbDeclareToTrace probDeclareToTrace = probDeclareToTraceService.safeFindById(probDeclareToTraceId);

        List<Declare> entities = Arrays.stream(response.constraints())
                .map(c -> new Declare(probDeclareToTrace, c))
                .toList();

        return mapToModel(saveAll(entities));
    }
}
