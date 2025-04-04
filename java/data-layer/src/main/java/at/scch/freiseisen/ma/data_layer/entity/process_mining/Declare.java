package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class Declare extends BaseEntity<String> {

    private String constraintTemplate;
    private Double probability;
    private Long nr;

//    @OneToOne
//    @JoinColumn(name = "trace_id")
//    private Trace trace; // FIXME how to keep trace association?

    @ManyToOne
    @JoinColumn(name = "prob_declare_id")
    @JsonBackReference("d_pd")
    private ProbDeclare probDeclare;

    public Declare(ProbDeclareToTrace probDeclareToTrace, String constraintTemplate) {
        this.id = UUID.randomUUID().toString();
        this.probability = 1d;
        this.nr = 1L;
        this.constraintTemplate = constraintTemplate;
        this.probDeclare = probDeclareToTrace.getProbDeclare();
//        this.trace = probDeclareToTrace.getTrace();
        this.updateDate = LocalDateTime.now();
        this.insertDate = LocalDateTime.now();
    }
}
