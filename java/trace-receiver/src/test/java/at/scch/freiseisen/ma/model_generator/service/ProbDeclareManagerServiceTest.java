package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareModel;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.model_generator.configuration.ModelGenerationConfig;
import at.scch.freiseisen.ma.model_generator.configuration.RestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class ProbDeclareManagerServiceTest {

    @Mock
    private PersistenceService persistenceService;

    @Autowired
    RestConfig restConfig;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    DeclareService declareService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ModelGenerationConfig modelGenerationConfig;

    private TraceCacheManager traceCacheManager;
    private final String testId = UUID.randomUUID().toString();

//    @Spy
//    @Autowired
//    @InjectMocks
    private ProbDeclareManagerService probDeclareManagerService;
//    private ProbDeclareModel probDeclareModel;

    @BeforeEach
    void setUp() {
        traceCacheManager = Mockito.spy(new TraceCacheManager(restConfig, restTemplate, persistenceService));
        probDeclareManagerService = Mockito.spy(
                new ProbDeclareManagerService(
                        restTemplate, restConfig, declareService, traceCacheManager, objectMapper,
                        modelGenerationConfig, persistenceService
                )
        );
        ProbDeclare probDeclare = new ProbDeclare();
        probDeclare.setId(testId);
        Mockito.when(persistenceService.persistProbDeclare(Mockito.any())).thenReturn(probDeclare);
    }

    @Test
    void testProbDeclareModelGeneration() {
//        SourceDetails sourceDetails = new SourceDetails();
//        sourceDetails.setPage(0);
//        sourceDetails.setSize(100);
//        sourceDetails.setSort("sourceFile"); // TODO adapt
//        sourceDetails.setSourceFile("/tmp/extraction10022699133263562863/traces_spans/AstroShop_Demo_Traces.json");
//        ProbDeclareModel init = probDeclareManagerService.generate(sourceDetails, 1239);
//        int segmentSize = (int) Math.ceil((double) 1239 / 4);
//        ProbDeclareModel[] partialModels = new ProbDeclareModel[4];
//        for(int i=0; i<segmentSize;) {
//            long processed = probDeclareManagerService.getCurrentNrTracesProcessed().get();
//            if (processed > 0 && processed % segmentSize == 0) {
//                partialModels[i] = probDeclareManagerService.getProbDeclareModel(init.id());
//                i++;
//
//            }
//        }
//        for (ProbDeclareModel probDeclareModel : partialModels) {
//            log.info(probDeclareModel.toString());
//        }
//        assertTrue(true);
    }

}
