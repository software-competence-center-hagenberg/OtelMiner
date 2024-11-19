package at.scch.freiseisen.ma.data_layer.repository.otel;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRepository extends BaseRepository<Trace, String> {

    @Query(name = "Trace.findDataOverview", nativeQuery = true)
    List<DataOverview> findDataOverview();

    @Transactional
    @Query("SELECT t FROM Trace t WHERE t.sourceFile = :sourceFile")
    Page<Trace> findBySourceFile(Pageable pageable, @Param("sourceFile") String sourceFile);
}
