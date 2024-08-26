package at.scch.freiseisen.ma.db_service.service.span;

import at.scch.freiseisen.ma.data_layer.entity.span.Span;
import at.scch.freiseisen.ma.data_layer.repository.span.SpanRepository;
import at.scch.freiseisen.ma.db_service.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class SpanService extends BaseService<SpanRepository, Span, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SpanService(SpanRepository repository) {
        super(repository);
    }
}
