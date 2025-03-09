package at.scch.freiseisen.ma.data_layer.dto;

// FIXME currently array of array of constraints but only one traceId --> only feasible if multiple traceIds
public record ConversionResponse(String traceId, String[] constraints) {
}
