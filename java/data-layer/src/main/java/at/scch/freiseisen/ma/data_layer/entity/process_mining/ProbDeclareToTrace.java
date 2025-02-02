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
    private String probDeclareId;
    @Id
    private String traceId;

    @ManyToOne
    @JoinColumn(name = "prob_declare_id", nullable = false)
    @JsonBackReference("pdt_pd")
    private ProbDeclare probDeclare;

    @ManyToOne
    @JoinColumn(name = "trace_id", nullable = false)
    @JsonBackReference("pdt_t")
    private Trace trace;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime insertDate;

    public ProbDeclareToTrace(ProbDeclare probDeclare, Trace trace) {
        this.probDeclare = probDeclare;
        this.trace = trace;
        this.insertDate = LocalDateTime.now();
    }

}
