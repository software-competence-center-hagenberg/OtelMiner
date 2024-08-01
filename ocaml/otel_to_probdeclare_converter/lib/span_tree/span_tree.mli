type span_tree_node

val create_span_trees :
  Opentelemetry_proto.Trace.resource_spans -> span_tree_node list
