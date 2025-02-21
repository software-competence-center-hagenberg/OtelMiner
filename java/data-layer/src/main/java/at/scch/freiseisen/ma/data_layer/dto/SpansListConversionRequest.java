package at.scch.freiseisen.ma.data_layer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpansListConversionRequest {
    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("spans")
    private String[] spans;
}