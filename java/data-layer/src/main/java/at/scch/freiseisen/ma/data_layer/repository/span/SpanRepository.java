package at.scch.freiseisen.ma.data_layer.repository.span;

import at.scch.freiseisen.ma.data_layer.entity.span.Span;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpanRepository extends BaseRepository<Span, String> {
}
