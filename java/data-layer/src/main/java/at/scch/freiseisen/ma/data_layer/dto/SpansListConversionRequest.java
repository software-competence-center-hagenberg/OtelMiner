package at.scch.freiseisen.ma.data_layer.dto;

import java.util.List;

public record SpansListConversionRequest(String traceId, List<String> spans) {
}
