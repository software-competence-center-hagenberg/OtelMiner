package at.scch.freiseisen.ma.data_layer.repository.otel;

import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpanRepository extends BaseRepository<Span, String> {
/*
@Query(value = """
            SELECT new at.scch.freiseisen.ma.data_layer.dto.TraceData(
                tr.id,
                array_agg(t.json)
            )
            FROM trace tr
            JOIN span t ON tr.id = t.trace_id
            WHERE tr.source_file = :sourceFile
            GROUP BY tr.id
            """, nativeQuery = true)
 */
//    @Query("""
//            select new at.scch.freiseisen.ma.data_layer.dto.TraceData(
//                tr.id,
//                array_agg (t.json)
//            )
//            from Trace tr
//            join tr.spans t
//            where tr.sourceFile = :sourceFile
//            group by t.id
//            """)
//    public List<TraceData> findTraceData(@Param("sourceFile") String sourceFile);
}
