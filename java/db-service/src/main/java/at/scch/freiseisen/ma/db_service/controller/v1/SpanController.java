package at.scch.freiseisen.ma.db_service.controller.v1;

import at.scch.freiseisen.ma.data_layer.entity.otel.Span;
import at.scch.freiseisen.ma.data_layer.repository.otel.SpanRepository;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import at.scch.freiseisen.ma.db_service.service.SpanService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("v1/spans")
@RestController
public class SpanController extends BaseController<SpanService, SpanRepository, Span, String> {
    public SpanController(SpanService service) {
        super(service);
    }
}
