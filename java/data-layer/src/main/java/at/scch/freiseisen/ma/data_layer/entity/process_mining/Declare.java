package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @Column(name = "constraint_template")
    private String constraintTemplate;

//    @OneToOne
//    @JoinColumn(name = "trace_id")
//    private Trace trace; // FIXME add DeclareToTrace Join Table

    @ManyToOne
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

    public Declare(ProbDeclareToTrace probDeclareToTrace, String constraintTemplate) {
//        this.id = UUID.randomUUID().toString();
        this.probability = 1d;
        this.nr = 1L;
        this.constraintTemplate = constraintTemplate;
        this.probDeclare = probDeclareToTrace.getProbDeclare();
//        this.trace = probDeclareToTrace.getTrace();
        this.updateDate = LocalDateTime.now();
        this.insertDate = LocalDateTime.now();
    }
}
