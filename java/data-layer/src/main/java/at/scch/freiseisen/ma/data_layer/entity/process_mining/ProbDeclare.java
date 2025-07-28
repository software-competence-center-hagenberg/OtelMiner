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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProbDeclare extends BaseEntity<String> {

    private boolean generating;

    @JsonManagedReference("d_pd")
    @OneToMany(mappedBy = "probDeclare", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Declare> declareList = new ArrayList<>();

    @JsonManagedReference("pdt_pd")
    @OneToMany(mappedBy = "probDeclare", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ProbDeclareToTrace> traces = new ArrayList<>();

    public ProbDeclare(String id) {
        this.id = id;
        this.generating = true;
        this.insertDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    public List<Declare> getCrispConstraints() {
        return declareList.stream().filter(d -> d.getProbability() == 1).toList();
    }

    public List<Declare> getProbabilityConstraints() {
        return declareList.stream().filter(d -> d.getProbability() != 1).toList();
    }
}
