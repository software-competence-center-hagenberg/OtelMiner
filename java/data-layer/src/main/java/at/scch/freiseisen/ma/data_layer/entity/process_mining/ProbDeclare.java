package at.scch.freiseisen.ma.data_layer.entity.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProbDeclare extends BaseEntity<String> {

    @JsonManagedReference
    @OneToMany(mappedBy = "probDeclare", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Declare> declareList;

    public List<Declare> getCrispConstraints() {
        return declareList.stream().filter(d -> d.getProbability() == 1).toList();
    }

    public List<Declare> getProbabilityConstraints() {
        return declareList.stream().filter(d -> d.getProbability() != 1).toList();
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "probDeclare", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProbDeclareToTrace> traces;

    public ProbDeclare(String id) {
        this.id = id;
        // TODO check
//        this.insertDate = LocalDateTime.now();
//        this.updateDate = LocalDateTime.now();
    }
}
