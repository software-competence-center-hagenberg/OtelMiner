package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(ProbDeclareToTraceId.class)
public class ProbDeclareToTrace {

    @Id
    @ManyToOne
    @JoinColumn(name = "prob_declare_id", nullable = false)
    private ProbDeclare probDeclare;

    @Id
    @ManyToOne
    @JoinColumn(name = "trace_id", nullable = false)
    private Trace trace;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime insertDate;

    public ProbDeclareToTrace(ProbDeclare probDeclare, Trace trace) {
        this.probDeclare = probDeclare;
        this.trace = trace;
        this.insertDate = LocalDateTime.now();
    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        ProbDeclareToTrace entity = (ProbDeclareToTrace) o;
//        return id.equals(entity.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id);
//    }

}
