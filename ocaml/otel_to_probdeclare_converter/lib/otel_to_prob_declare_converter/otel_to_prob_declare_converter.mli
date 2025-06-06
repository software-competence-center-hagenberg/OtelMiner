type mapping_conf = {
  p : string list;
  a : Util.StringSet.t;
  c : Util.DeclareSet.t;
}

val convert_resource_spans :
  Opentelemetry_proto.Trace.resource_spans list -> Declare.t list list

val convert_trace_spans_for_single_trace :
  Opentelemetry_proto.Trace.span list -> Declare.t list


val convert_trace_spans_for_single_trace_without_parent_span_ids :
  Opentelemetry_proto.Trace.span list -> Declare.t list

val convert_trace_spans_for_multiple_traces :
  Opentelemetry_proto.Trace.span list -> Declare.t list list

val determine_relation : string -> string -> string list -> Declare.t option
val map_relations : string list -> Util.DeclareSet.t
val map_choices : string list -> Util.DeclareSet.t
val initialize_conf : Span_tree.span_tree_node -> mapping_conf
val map_to_declare : Span_tree.span_tree_node -> Declare.t list
