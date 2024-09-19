package at.scch.freiseisen.ma.data_layer.repository.otel;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceRepository extends BaseRepository<Trace, String> {
}
