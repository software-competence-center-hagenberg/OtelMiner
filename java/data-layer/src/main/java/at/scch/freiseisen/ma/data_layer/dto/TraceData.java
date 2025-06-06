package at.scch.freiseisen.ma.data_layer.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceData {
    private String traceId;
    private int nrNodes;
    private List<String> spans;
    private String traceDataType;
}
