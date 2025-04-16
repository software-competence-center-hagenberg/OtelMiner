package at.scch.freiseisen.ma.data_layer.service;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.dto.SourceDetails;
import at.scch.freiseisen.ma.data_layer.dto.TraceData;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.otel.TraceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TraceService extends BaseService<TraceRepository, Trace, String> {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TraceService(TraceRepository repository) {
        super(repository);
    }

    public List<DataOverview> findDataOverview() {
        return repository.findDataOverview();
    }

    public SourceDetails findSourceDetails(SourceDetails sourceDetails) {
        Page<Trace> tracesForSourceFile = buildPageableAndRequestBySourceFile(sourceDetails);
        List<TraceData> traces = tracesForSourceFile.getContent()
                .stream()
                .map(t -> new TraceData(t.getId(), t.getNrNodes(), t.getSpansAsJson()))
                .toList();
        sourceDetails.setTraces(traces);
        sourceDetails.setTotalPages(tracesForSourceFile.getTotalPages());
        return sourceDetails;
    }

    public Page<Trace> findBySourceFile(SourceDetails sourceDetails) {
        return buildPageableAndRequestBySourceFile(sourceDetails);
    }

    private Page<Trace> buildPageableAndRequestBySourceFile(SourceDetails sourceDetails) {
        Pageable pageable = PageRequest.of(
                sourceDetails.getPage(),
                sourceDetails.getSize(),
                Sort.by(sourceDetails.getSort())
        );
        return repository.findBySourceFile(pageable, sourceDetails.getSourceFile());
    }

    public Page<Trace> findBySourceFile(String sourceFile, int page, int size, Sort by) {
        Pageable pageable = PageRequest.of(page, size, by);
        return repository.findBySourceFile(pageable, sourceFile);
    }
}
