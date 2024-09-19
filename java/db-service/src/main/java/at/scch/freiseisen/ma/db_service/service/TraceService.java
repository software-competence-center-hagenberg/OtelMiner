package at.scch.freiseisen.ma.db_service.service;

import at.scch.freiseisen.ma.data_layer.dto.DataOverview;
import at.scch.freiseisen.ma.data_layer.entity.otel.Trace;
import at.scch.freiseisen.ma.data_layer.repository.otel.TraceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
