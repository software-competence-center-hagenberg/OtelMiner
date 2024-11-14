package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.repository.process_mining.DeclareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeclareService extends BaseService<DeclareRepository, Declare, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public DeclareService(DeclareRepository repository) {
        super(repository);
    }
}
