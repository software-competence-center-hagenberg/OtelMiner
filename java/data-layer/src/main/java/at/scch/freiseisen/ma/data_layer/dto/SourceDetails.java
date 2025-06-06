package at.scch.freiseisen.ma.data_layer.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceDetails {
    private String sourceFile;
    private List<TraceData> traces;
    private int page;
    private int size;
    private int totalPages;
    private int totalElements;
    private String sort;

    public SourceDetails(String sourceFile, int page, int size, String sort) {
        this.sourceFile = sourceFile;
        this.page = page;
        this.size = size;
        this.sort = sort;
    }
}
