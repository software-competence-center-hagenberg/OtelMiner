package at.scch.freiseisen.ma.data_layer.entity.otel;

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

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Trace extends BaseEntity<String> {
    private Integer nrNodes;
    private String sourceFile;

    @JsonManagedReference
    @OneToMany(mappedBy = "trace", fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Span> spans;

}
