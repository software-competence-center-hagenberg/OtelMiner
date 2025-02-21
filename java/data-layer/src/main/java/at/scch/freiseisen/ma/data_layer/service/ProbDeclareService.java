package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.ProbDeclareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProbDeclareService extends BaseService<ProbDeclareRepository, ProbDeclare, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ProbDeclareService(ProbDeclareRepository repository) {
        super(repository);
    }
}
