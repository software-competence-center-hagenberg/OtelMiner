package at.scch.freiseisen.ma.model_generator.rest;

import at.scch.freiseisen.ma.commons.TraceDataType;
import at.scch.freiseisen.ma.model_generator.service.ResourceSpansService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CollectorController {
    private final ResourceSpansService resourceSpansService;

    @PostMapping("/traces")
    public void receiveTraces(@RequestBody String trace) {
        log.info("trace:\n{}", trace);
        resourceSpansService.transformAndPipe(trace, TraceDataType.RESOURCE_SPANS);
    }
}
