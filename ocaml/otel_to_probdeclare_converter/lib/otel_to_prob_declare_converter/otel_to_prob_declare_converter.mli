 type ltl

  val convert : Opentelemetry_proto.Trace.resource_spans list -> ltl list
  val get_ltl_string : ltl list -> string