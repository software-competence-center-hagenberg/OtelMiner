package at.scch.freiseisen.ma.data_layer.entity.otel;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@NamedNativeQuery(
        name = "Trace.findDataOverview",
        query = """
                    SELECT
                        tr.source_file AS sourceFile,
                        COUNT(DISTINCT tr.id) AS nrTraces,
                        ARRAY_AGG(DISTINCT tr.nr_nodes) AS nrNodes
                    FROM public.trace tr
                             JOIN public.span t ON tr.id = t.trace_id
                    GROUP BY tr.source_file
                """,
        resultSetMapping = "DataOverviewMapping"
)
@SqlResultSetMapping(
        name = "DataOverviewMapping",
        classes = @ConstructorResult(
                targetClass = DataOverview.class,
                columns = {
                        @ColumnResult(name = "sourceFile", type = String.class),
                        @ColumnResult(name = "nrTraces", type = Integer.class),
                        @ColumnResult(name = "nrNodes", type = Integer[].class)
                }
        )
)
public class Trace extends BaseEntity<String> {
    private Integer nrNodes;
    private String sourceFile;

    @JsonManagedReference
    @OneToMany(mappedBy = "trace", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Span> spans;

}
