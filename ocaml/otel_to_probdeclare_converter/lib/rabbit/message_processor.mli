type result = { trace_id : string; constraints : Declare.t list list }

val process : Util.trace_string_type -> string -> result
val result_to_json_string : result -> string