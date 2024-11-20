package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Declare extends BaseEntity<String> {

    private String constraintTemplate;
    private Float probability;

    @OneToOne
    @JoinColumn(name = "trace_id")
    private Trace trace;

    @ManyToOne
    @JoinColumn(name = "prob_declare_id")
    @JsonBackReference
    private ProbDeclare probDeclare;
}
