package at.scch.freiseisen.ma.db_service.controller;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.service.DeclareService;
import at.scch.freiseisen.ma.data_layer.service.ProbDeclareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TestProbDeclareModel {
    @Autowired
    private ProbDeclareService probDeclareService;
    @Autowired
    private DeclareService declareService;

    /**
     * <pre>
     *     Requirements:
     *      - The database is up and running.
     *      - There exist two generated models of the same data set in the database.
     *     If this is not the case the test will fail!
     *     Make sure to initialize the database, generate two models and fill in their IDs in the test.
     *     Further adapt the tests according to your data sets.
     * </pre>
     */
    @Test
    void testAllProbDeclareModelEqualities() {
        testDynatraceModelEquality();
        testTrainTicketModelEquality();
    }

    void testDynatraceModelEquality() {
        String id1 = "82530a30-f0b0-4b99-aa2c-9656875cc032";
//        String id2 = "11d8cd1b-7be5-4a06-8609-eb6829898ceb";
        String id2 = "bbc86809-157b-4f80-8a77-b9e86406f0a9";
        testProbDeclareModelEquality(id1, id2);
    }

    private void testTrainTicketModelEquality() {
        String id1 = "a4be77f3-70aa-40bb-b789-747cad8318e1";
        String id2 = "c8456eaa-8835-49ce-85f6-b8ff0fac5d99";
        testProbDeclareModelEquality(id1, id2);
    }

    void testProbDeclareModelEquality(String id1, String id2) {
        ProbDeclare probDeclare1 = probDeclareService.findById(id1);
        ProbDeclare probDeclare2 = probDeclareService.findById(id2);
        List<Declare> model1 = declareService.findAllByProbDeclare(probDeclare1);
        List<Declare> model2 = declareService.findAllByProbDeclare(probDeclare2);
        model1.sort(Comparator.comparing(Declare::getConstraintTemplate));
        model2.sort(Comparator.comparing(Declare::getConstraintTemplate));
        assertEquals(model1, model2);
    }


}
