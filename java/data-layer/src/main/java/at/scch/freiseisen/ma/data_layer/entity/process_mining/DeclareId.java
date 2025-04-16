package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class DeclareId implements Serializable {
    @Column(name = "prob_declare_id")
    private String probDeclareId;

    @Column(name = "constraint_template")
    private String constraintTemplate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeclareId that = (DeclareId) o;
        return Objects.equals(probDeclareId, that.probDeclareId) &&
               Objects.equals(constraintTemplate, that.constraintTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(probDeclareId, constraintTemplate);
    }
}
