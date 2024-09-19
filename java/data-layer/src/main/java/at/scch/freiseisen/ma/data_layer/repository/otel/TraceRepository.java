package at.scch.freiseisen.ma.data_layer.repository.otel;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRepository extends BaseRepository<Trace, String> {

    @Query(name = "Trace.findDataOverview", nativeQuery = true)
    List<DataOverview> findDataOverview();

}
