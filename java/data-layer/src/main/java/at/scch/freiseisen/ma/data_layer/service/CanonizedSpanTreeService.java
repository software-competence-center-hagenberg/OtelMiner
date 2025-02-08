package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.entity.pre_processing.CanonizedSpanTree;
import at.scch.freiseisen.ma.data_layer.repository.pre_processing.CanonizedSpanTreeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CanonizedSpanTreeService extends BaseService<CanonizedSpanTreeRepository, CanonizedSpanTree, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CanonizedSpanTreeService(CanonizedSpanTreeRepository repository) {
        super(repository);
    }
}
