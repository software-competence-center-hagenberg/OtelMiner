package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(name = "prob_declare_id")
    private String probDeclareId;
    @Id
    @Column(name = "trace_id")
    private String traceId;

    @ManyToOne
    @MapsId("probDeclareId")
    @JoinColumn(name = "prob_declare_id", nullable = false, insertable = false, updatable = false)
    @JsonBackReference("pdt_pd")
    private ProbDeclare probDeclare;

    @ManyToOne
    @MapsId("traceId")
    @JoinColumn(name = "trace_id", nullable = false, insertable = false, updatable = false)
    @JsonBackReference("pdt_t")
    private Trace trace;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime insertDate;

    public ProbDeclareToTrace(ProbDeclare probDeclare, Trace trace) {
        this.probDeclare = probDeclare;
        this.trace = trace;
        this.insertDate = LocalDateTime.now();
        this.probDeclareId = probDeclare.getId();
        this.traceId = trace.getId();
    }

}
