package at.scch.freiseisen.ma.data_layer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpanTreeNodeConversionResponse {
    private String traceId;

    private List<SpanTreeNode> spanTrees;
}
