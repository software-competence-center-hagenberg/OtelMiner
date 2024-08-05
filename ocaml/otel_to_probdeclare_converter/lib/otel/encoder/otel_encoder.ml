(*open Yojson.Basic*)
open Opentelemetry_proto

(* Encode an array_value to JSON *)
let rec encode_array_value (array_value : Common.array_value) : Yojson.Basic.t =
  `List (List.map encode_any_value array_value.values)

(* Encode a key_value_list to JSON *)
and encode_key_value_list (kvlist : Common.key_value_list) : Yojson.Basic.t =
  `Assoc
    (List.map
       (fun (key_value : Common.key_value) ->
         (key_value.key, encode_any_value (Option.get key_value.value)))
       kvlist.values)

(* Encode an any_value to JSON *)
and encode_any_value (any_value : Common.any_value) : Yojson.Basic.t =
  match any_value with
  | Bytes_value b -> `String (Base64.encode_exn (Bytes.to_string b))
  | String_value s -> `String s
  | Bool_value b -> `Bool b
  | Int_value i -> `Int (Int64.to_int i)
  | Double_value f -> `Float f
  | Array_value av -> encode_array_value av
  | Kvlist_value kv -> encode_key_value_list kv

(* Encode a KeyValue to JSON *)
let encode_attribute (attribute : Common.key_value) : Yojson.Basic.t =
  `Assoc
    [
      ("key", `String attribute.key);
      ( "value",
        match attribute.value with
        | Some v -> `Assoc [ ("stringValue", encode_any_value v) ]
        | None -> `Null );
    ]

let encode_span_kind (sk : Trace.span_span_kind) : int =
  match sk with
  | Span_kind_internal -> 1
  | Span_kind_server -> 2
  | Span_kind_client -> 3
  | Span_kind_producer -> 4
  | Span_kind_consumer -> 5
  | Span_kind_unspecified -> 0

(* Encode a Span to JSON *)
let encode_span (span : Trace.span) : Yojson.Basic.t =
  `Assoc
    [
      ("traceId", `String (Bytes.to_string span.trace_id));
      ("spanId", `String (Bytes.to_string span.span_id));
      ("parentSpanId", `String (Bytes.to_string span.parent_span_id));
      ("name", `String span.name);
      ("kind", `Int (encode_span_kind span.kind));
      ("startTimeUnixNano", `String (Int64.to_string span.start_time_unix_nano));
      ("endTimeUnixNano", `String (Int64.to_string span.end_time_unix_nano));
      ("attributes", `List (List.map encode_attribute span.attributes));
      (* Add additional fields if necessary *)
    ]

(* Encode an InstrumentationScope to JSON *)
let encode_scope (scope : Common.instrumentation_scope) : Yojson.Basic.t =
  `Assoc
    [
      ("name", `String scope.name);
      (* Optional: Add version and attributes if needed *)
    ]

(* Encode a ScopeSpans to JSON *)
let encode_scope_spans (scope_spans : Trace.scope_spans) : Yojson.Basic.t =
  `Assoc
    [
      ("scope", encode_scope (Option.get scope_spans.scope));
      ("spans", `List (List.map encode_span scope_spans.spans));
      (* Optional: Add schema_url if needed *)
    ]

(* Encode a Resource to JSON *)
let encode_resource (resource : Resource.resource) : Yojson.Basic.t =
  `Assoc
    [
      ("attributes", `List (List.map encode_attribute resource.attributes));
      (* Optional: Add additional fields if needed *)
    ]

(* Encode a ResourceSpans to JSON *)
let encode_resource_spans (resource_spans : Trace.resource_spans) :
    Yojson.Basic.t =
  `Assoc
    [
      ("resource", encode_resource (Option.get resource_spans.resource));
      ( "scopeSpans",
        `List (List.map encode_scope_spans resource_spans.scope_spans) );
      ("schemaUrl", `String resource_spans.schema_url);
    ]

(* Encode a list of ResourceSpans to a JSON string *)
let encode (resource_spans_list : Trace.resource_spans list) : string =
  `Assoc
    [
      ( "resourceSpans",
        `List (List.map encode_resource_spans resource_spans_list) );
    ]
  |> Yojson.Basic.to_string
