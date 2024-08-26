package at.scch.freiseisen.ma.db_service.controller.span;

import at.scch.freiseisen.ma.data_layer.entity.span.Span;
import at.scch.freiseisen.ma.data_layer.repository.span.SpanRepository;
import at.scch.freiseisen.ma.db_service.controller.BaseController;
import at.scch.freiseisen.ma.db_service.service.span.SpanService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpanController extends BaseController<SpanService, SpanRepository, Span, String> {
    public SpanController(SpanService service) {
        super(service);
    }
}
