package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareConstraintModelEntry;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@IdClass(DeclareId.class)
public class Declare {

    @Id
    @Column(name = "prob_declare_id")
    private String probDeclareId;

    @Id
    @Column(name = "constraint_template", length = 500)
    private String constraintTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("probDeclareId")
    @JoinColumn(name = "prob_declare_id", nullable = false, insertable = false, updatable = false)
    @JsonBackReference("d_pd")
    private ProbDeclare probDeclare;

    private Double probability;
    private Long nr;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime insertDate;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected LocalDateTime updateDate;

    public Declare(ProbDeclareConstraintModelEntry e, ProbDeclare probDeclare) {
        this.probability = e.getProbability();
        this.nr = e.getNr();
        this.constraintTemplate = e.getConstraintTemplate();
        this.insertDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
        this.probDeclare = probDeclare;
        this.probDeclareId = probDeclare.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Declare declare = (Declare) o;
        return Objects.equals(getConstraintTemplate(), declare.getConstraintTemplate())
               && Objects.equals(getProbability(), declare.getProbability())
               && Objects.equals(getNr(), declare.getNr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConstraintTemplate(), getProbability(), getNr());
    }
}
