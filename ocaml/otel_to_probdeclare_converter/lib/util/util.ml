module DeclareSet = Set.Make (Declare)
module StringSet = Set.Make (String)

let load_json_from_file ?(prefix : string = "/../../../../../test/")
    (file_name : string) : Yojson.Basic.t =
  Yojson.Basic.from_file (Sys.getcwd () ^ prefix ^ file_name)

type trace_type =
  | JAEGER_TRACE
  | JAEGER_SPANS_LIST
  | OTEL_SPANS_LIST
  | RESOURCE_SPANS

let trace_string_type_to_string (t: trace_type) : string =
  match t with
  | JAEGER_TRACE -> "JAEGER_TRACE"
  | JAEGER_SPANS_LIST -> "JAEGER_SPANS_LIST"
  | OTEL_SPANS_LIST -> "OTEL_SPANS_LIST"
  | RESOURCE_SPANS -> "RESOURCE_SPANS"