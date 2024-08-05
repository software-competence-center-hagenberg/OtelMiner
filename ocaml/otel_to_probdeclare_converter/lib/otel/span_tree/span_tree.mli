type span_tree_node = {
  span : Opentelemetry_proto.Trace.span;
  children : span_tree_node list;
}

val create_span_trees :
  Opentelemetry_proto.Trace.resource_spans -> span_tree_node list

(*val string_of_span_tree : ?indent_level:int -> span_tree_node -> string*)
