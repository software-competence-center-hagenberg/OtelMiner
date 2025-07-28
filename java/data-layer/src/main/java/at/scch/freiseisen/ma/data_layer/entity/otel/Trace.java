package at.scch.freiseisen.ma.data_layer.entity.otel;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
                        t.source_file AS sourceFile,
                        COUNT(DISTINCT t.id) AS nrTraces,
                        ARRAY_AGG(DISTINCT t.nr_nodes) AS nrNodes
                    FROM public.trace t
                             JOIN public.span s ON t.id = s.trace_id
                    GROUP BY t.source_file
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
    private String traceDataType;

    @JsonManagedReference("s_t")
    @OneToMany(mappedBy = "trace", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE}, orphanRemoval = false)
    private List<Span> spans;

    @JsonManagedReference("pdt_t")
    @OneToMany(mappedBy = "trace", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = false)
    private List<ProbDeclareToTrace> probDeclareToTraces;

    @JsonIgnore
    public List<String> getSpansAsJson() {
        return spans.stream()
                .map(Span::getJson)
                .toList();
    }
}
