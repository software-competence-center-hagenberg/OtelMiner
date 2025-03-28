package at.scch.freiseisen.ma.trace_collector.model;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProbDeclareGenerationDTO {
    private int currentPage;
    private int totalPages;
    private ArrayList<Trace> traces;

    public ProbDeclareGenerationDTO(Page<Trace> page) {
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.traces = new ArrayList<>(page.getContent());
    }

    public boolean hasContent() {
        return !traces.isEmpty();
    }

    public Trace getNext() {
        return traces.removeFirst();
    }

    public boolean hasMorePages() {
        return currentPage < totalPages;
    }
}
