type result = { trace_id : string; span_trees : Span_tree.span_tree_node list }

val process : Util.trace_string_type -> string -> result
val result_to_json_string : result -> string