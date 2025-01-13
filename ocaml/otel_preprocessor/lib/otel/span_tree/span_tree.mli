type span_tree_node = {
  span : Opentelemetry_proto.Trace.span;
  children : span_tree_node list;
}

val generate_span_trees_from_resource_spans :
  Opentelemetry_proto.Trace.resource_spans -> span_tree_node list

val generate_span_trees_from_spans :
  Opentelemetry_proto.Trace.span list -> span_tree_node list

val generate_nodes :
  Opentelemetry_proto.Trace.span list ->
  span_tree_node list * span_tree_node list

val pp_span_tree : Format.formatter -> span_tree_node -> unit

val string_of_nodes: span_tree_node list -> string

val list_to_json_string : span_tree_node list -> string