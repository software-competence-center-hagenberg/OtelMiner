package at.scch.freiseisen.ma.data_layer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MinimalSpan {

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("span_id")
    private String spanId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parent_span_id")
    private String parentSpanId;
}
