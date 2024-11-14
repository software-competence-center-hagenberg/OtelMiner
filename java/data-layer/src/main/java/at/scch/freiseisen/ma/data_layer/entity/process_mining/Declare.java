package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
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

    private String constraint;

    @OneToOne(mappedBy = "span", fetch = FetchType.EAGER)
    private Span span;

    @ManyToOne
    @JoinColumn(name = "prob_declare_id")
    @JsonBackReference
    private ProbDeclare probDeclare;
}
