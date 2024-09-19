package at.scch.freiseisen.ma.data_layer.repository.otel;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpanRepository extends BaseRepository<Span, String> {
}
