package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProbDeclareToTrace extends BaseEntity<String> {

    @ManyToOne
    @JoinColumn(name = "prob_declare_id")
    private ProbDeclare probDeclare;

    @ManyToOne
    @JoinColumn(name = "trace_id")
    private Trace trace;

}
