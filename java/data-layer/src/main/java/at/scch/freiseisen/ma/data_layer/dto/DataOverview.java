package at.scch.freiseisen.ma.data_layer.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataOverview {
    private String sourceFile;
    private Integer nrTraces;
    private Integer[] nrNodes;
}
