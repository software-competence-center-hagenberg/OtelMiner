val decode_resources_spans_string :
  string -> Opentelemetry_proto.Trace.resource_spans list

val decode_trace_string : string -> Opentelemetry_proto.Trace.span list
val decode_trace_span : Yojson.Basic.t -> Opentelemetry_proto.Trace.span
val decode_jaeger_trace_string : string -> Opentelemetry_proto.Trace.span list
val decode_jaeger_trace: Yojson.Basic.t -> Opentelemetry_proto.Trace.span list
val decode_jaeger_trace_span : Yojson.Basic.t -> Opentelemetry_proto.Trace.span
