package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.otel.TraceRepository;
import org.springframework.stereotype.Service;

@Service
public class TraceService extends BaseService<TraceRepository, Trace, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TraceService(TraceRepository repository) {
        super(repository);
    }
}
