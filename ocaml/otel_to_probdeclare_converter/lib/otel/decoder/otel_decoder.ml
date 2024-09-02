open Yojson.Basic.Util
open Opentelemetry_proto

(* Decode an array_value from JSON *)
let rec decode_array_value json =
  Common.default_array_value
    ~values:(json |> to_list |> List.map decode_any_value)
    ()

(* Decode a key_value_list from JSON *)
and decode_key_value_list json =
  Common.default_key_value_list
    ~values:
      (json |> to_assoc
      |> List.map (fun (key, value) ->
             Common.make_key_value ~key
               ~value:(Some (decode_any_value value))
               ()))
    ()

(* Decode an any_value from JSON *)
and decode_any_value json : Common.any_value =
  match json with
  | `String s when String.length s mod 4 = 0 -> (
      (* Check if the string can be decoded as base64 for bytes *)
      try Bytes_value (Base64.decode_exn s |> Bytes.of_string)
      with _ -> String_value s (* Fallback if base64 decoding fails *))
  | `String s -> String_value s
  | `Bool b -> Bool_value b
  | `Int i -> Int_value (Int64.of_int i)
  | `Float f -> Double_value f
  | `List l -> Array_value (decode_array_value (`List l))
  | `Assoc a -> Kvlist_value (decode_key_value_list (`Assoc a))
  | _ -> failwith "Unsupported JSON value for any_value"


(* Decode a attributes to keyValue list from JSON *)
let decode_attributes json =
  json |> to_assoc
  |> List.map (fun (k, v) ->
         Common.make_key_value ~key:k ~value:(Some (decode_any_value v)) ())

(* Decode a resource spans attribute to keyValue from JSON *)
let decode_resource_spans_attribute json =
  Common.make_key_value
    ~key:(json |> member "key" |> to_string)
    ~value:
      (Some (decode_any_value (json |> member "value" |> member "stringValue")))
    ()

let decode_span_kind_from_int sk : Trace.span_span_kind =
  match sk with
  | 1 -> Span_kind_internal
  | 2 -> Span_kind_server
  | 3 -> Span_kind_client
  | 4 -> Span_kind_producer
  | 5 -> Span_kind_consumer
  | _ -> Span_kind_unspecified

let decode_span_kind sk : Trace.span_span_kind =
  match sk with
  | "INTERNAL" -> Span_kind_internal
  | "SERVER" -> Span_kind_server
  | "CLIENT" -> Span_kind_client
  | "PRODUCER" -> Span_kind_producer
  | "CONSUMER" -> Span_kind_consumer
  | _ -> Span_kind_unspecified

(*let decode_status_code s : Trace.status_status_code=
  match s with
  | "STATUS_CODE_OK" -> Status_code_ok
  | "STATUS_CODE_ERROR" -> Status_code_error
  | _ -> Status_code_unset*)

(*let decode_status s m =
  Some (
    Trace.make_status
      ~code: (decode_status_code s)
      ~message: m (* Optional: Add if status message is needed *)
    ()
  )*)

(* Decode a Scope Span from JSON *)
let decode_scope_span json : Trace.span =
  Trace.default_span
    ~trace_id:(Bytes.of_string (json |> member "traceId" |> to_string))
    ~span_id:(Bytes.of_string (json |> member "spanId" |> to_string))
    ~parent_span_id:
      (Bytes.of_string (json |> member "parentSpanId" |> to_string))
      (*~flags: (json |> member "flags" |> to_int)*)
    ~name:(json |> member "name" |> to_string)
    ~kind:(json |> member "kind" |> to_int |> decode_span_kind_from_int)
    ~start_time_unix_nano:
      (Int64.of_string (json |> member "startTimeUnixNano" |> to_string))
    ~end_time_unix_nano:
      (Int64.of_string (json |> member "endTimeUnixNano" |> to_string))
    ~attributes:
      (json |> member "attributes" |> to_list |> List.map decode_resource_spans_attribute)
      (*~status: (decode_status (json |> member "status" |> to_string) "")*)
    ~events:[] ~links:[] ~dropped_attributes_count:(Int32.of_int 0)
    ~dropped_events_count:(Int32.of_int 0) ~dropped_links_count:(Int32.of_int 0)
    ()

let decode_trace_span json =
  let trace_id = json |> member "traceId" |> to_string in
  let span_id = json |> member "spanId" |> to_string in
  let parent_span_id =
    json |> member "parentSpanId" |> to_string_option
    |> Option.value ~default:""
  in
  let name = json |> member "name" |> to_string in
  let kind =
    json |> member "kind" |> to_string_option
    |> Option.map decode_span_kind
    |> Option.value ~default:Trace.Span_kind_unspecified
  in
  let start_time_unix_nano =
    json |> member "start" |> to_int_option |> Option.map Int64.of_int
    |> Option.value ~default:Int64.zero
  in
  let end_time_unix_nano =
    json |> member "end" |> to_int_option |> Option.map Int64.of_int
    |> Option.value ~default:Int64.zero
  in
  let attributes =
    json |> member "attributes" |> decode_attributes
  in
  Trace.default_span ~trace_id:(Bytes.of_string trace_id)
    ~span_id:(Bytes.of_string span_id)
    ~parent_span_id:(Bytes.of_string parent_span_id)
    ~name ~kind ~start_time_unix_nano ~end_time_unix_nano ~attributes ~events:[]
    ~links:[] ~dropped_attributes_count:(Int32.of_int 0)
    ~dropped_events_count:(Int32.of_int 0) ~dropped_links_count:(Int32.of_int 0)
    ()

(* Decode an InstrumentationScope from JSON *)
let decode_scope json =
  Some
    (Common.make_instrumentation_scope
       ~name:(json |> member "name" |> to_string)
       ~version:"" (* Optional: Add if version is needed *)
       ~attributes:[] (* Optional: Add attributes if needed *)
       ~dropped_attributes_count:(Int32.of_int 0) ())

(* Decode a ScopeSpans from JSON *)
let decode_scope_spans json =
  Trace.make_scope_spans
    ~scope:(json |> member "scope" |> decode_scope)
    ~spans:(json |> member "spans" |> to_list |> List.map decode_scope_span)
    ~schema_url:"" (* Optional: Add if schema URL is needed *) ()

(* Decode a Resource from JSON *)
let decode_resource json =
  Some
    (Resource.make_resource
       ~attributes:
         (json |> member "attributes" |> to_list |> List.map decode_resource_spans_attribute)
       ~dropped_attributes_count:(Int32.of_int 0) ())

(* Decode a ResourceSpans from JSON *)
let decode_resource_spans json =
  Trace.make_resource_spans
    ~resource:(json |> member "resource" |> decode_resource)
    ~scope_spans:
      (json |> member "scopeSpans" |> to_list |> List.map decode_scope_spans)
    ~schema_url:(json |> member "schemaUrl" |> to_string)
    ()

(* Decode the JSON string into a ResourceSpans object *)
let decode_resources_spans_string json_string : Trace.resource_spans list =
  let json = Yojson.Basic.from_string json_string in
  let rs = json |> member "resourceSpans" |> to_list in
  List.map decode_resource_spans rs

let decode_trace_string json_string : Trace.span list =
  let json = Yojson.Basic.from_string json_string in
  let trace_spans = json |> to_list in
  List.map decode_trace_span trace_spans
