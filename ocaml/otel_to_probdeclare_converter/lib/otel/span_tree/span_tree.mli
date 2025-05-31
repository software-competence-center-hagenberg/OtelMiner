type span_tree_node = {
  span : Opentelemetry_proto.Trace.span;
  children : span_tree_node list;
}

val generate_span_trees_from_resource_spans :
  Opentelemetry_proto.Trace.resource_spans -> span_tree_node list

val generate_span_tree_from_spans_for_single_trace :
  Opentelemetry_proto.Trace.span list -> span_tree_node

val generate_span_trees_from_spans_for_multiple_traces :
  Opentelemetry_proto.Trace.span list -> span_tree_node list

val generate_nodes_for_single_trace :
  Opentelemetry_proto.Trace.span list -> span_tree_node option * span_tree_node list

val generate_nodes_for_multiple_traces :
  Opentelemetry_proto.Trace.span list ->
  span_tree_node list * span_tree_node list

val pp_span_tree : Format.formatter -> span_tree_node -> unit
val string_of_nodes : span_tree_node list -> string
