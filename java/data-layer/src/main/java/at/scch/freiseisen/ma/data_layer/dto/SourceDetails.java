package at.scch.freiseisen.ma.data_layer.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceDetails {
    private TraceData[] traces;
}
