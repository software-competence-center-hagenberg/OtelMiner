type trace_model = { trace_id : string; constraints : Declare.t list }

type traces_model = {
  trace_ids : string list;
  constraints : Declare.t list list;
}

val process_trace : Util.trace_type -> string -> trace_model
val process_traces : Util.trace_type -> string -> traces_model
val trace_model_to_json_string : trace_model -> string
val traces_model_to_json_string : traces_model -> string
