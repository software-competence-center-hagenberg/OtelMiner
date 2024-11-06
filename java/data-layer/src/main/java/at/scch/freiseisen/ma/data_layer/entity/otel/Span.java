package at.scch.freiseisen.ma.data_layer.entity.otel;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
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
public class Span extends BaseEntity<String> {
    private String parentId;
    @Lob
    private String json;

    @ManyToOne
    @JoinColumn(name = "trace_id")
    @JsonBackReference
    private Trace trace;
}
