package at.scch.freiseisen.ma.data_layer.entity.span;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
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
}
