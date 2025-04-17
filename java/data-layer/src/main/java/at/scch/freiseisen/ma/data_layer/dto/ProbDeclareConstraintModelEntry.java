package at.scch.freiseisen.ma.data_layer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProbDeclareConstraintModelEntry implements Serializable, Cloneable {
    private String constraintTemplate;
    private Double probability;
    private Long nr;

    @Override
    public ProbDeclareConstraintModelEntry clone() {
        ProbDeclareConstraintModelEntry entry = new ProbDeclareConstraintModelEntry();
        entry.constraintTemplate = this.constraintTemplate;
        entry.probability = this.probability;
        entry.nr = this.nr;
        return entry;
    }

    public void increment() {
        nr++;
    }
}
