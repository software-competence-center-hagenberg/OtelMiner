package at.scch.freiseisen.ma.model_generator.model;

import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

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
        return !traces.isEmpty() ? traces.removeFirst() : null;
    }

    public boolean hasMorePages() {
        return currentPage < totalPages-1;
    }

    public List<Trace> getNext(int batchSize) {
        List<Trace> subList = traces.subList(0, Math.min(traces.size(), batchSize));
        traces.removeAll(subList);
        return subList;
    }
}
