package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class ProbDeclareToTraceId implements Serializable {
    @Column(name = "prob_declare_id")
    private String probDeclareId;

    @Column(name = "trace_id")
    private String traceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProbDeclareToTraceId that = (ProbDeclareToTraceId) o;
        return Objects.equals(probDeclareId, that.probDeclareId) &&
               Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(probDeclareId, traceId);
    }
}
