package at.scch.freiseisen.ma.model_generator.service;

import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class ProbDeclareManagerServiceTest {

    @Mock
    private TraceCacheManager mockTraceCacheManager;
    @Mock
    private PersistenceService mockPersistenceService;

    @Autowired
    @InjectMocks
    private ProbDeclareManagerService probDeclareManagerService;

    @Autowired
    private TraceCacheManager traceCacheManager;

    @Test
    void testProbDeclareModelGeneration() {
        SourceDetails sourceDetails = new SourceDetails();
        sourceDetails.setPage(0);
        sourceDetails.setSourceFile("/tmp/extraction10022699133263562863/traces_spans/AstroShop_Demo_Traces.json");
//        Mockito.when(mockTraceCacheManager.start(Mockito.any(), Mockito.anyString()))
//                .then(traceCacheManager.start(sourceDetails, "test"));
        // TODO implement
    }

}
