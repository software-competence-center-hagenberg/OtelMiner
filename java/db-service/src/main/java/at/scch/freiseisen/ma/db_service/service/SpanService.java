package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.repository.otel.SpanRepository;
import org.springframework.stereotype.Service;

@Service
public class SpanService extends BaseService<SpanRepository, Span, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SpanService(SpanRepository repository) {
        super(repository);
    }
}
