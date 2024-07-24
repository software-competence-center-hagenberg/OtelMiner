open Yojson.Basic.Util
open Opentelemetry_proto
(*open Base64*)
module type Otel_decoder = sig

  val decode : string -> Trace.resource_spans
end

(* Define the JsonDecoder module *)
module Decoder: Otel_decoder = struct
(* Decode an array_value from JSON *)
  let rec decode_array_value json =
    Common.default_array_value ~values: (json |> to_list |> List.map decode_any_value)
    ()
  
  (* Decode a key_value_list from JSON *)
  and decode_key_value_list json =
    Common.default_key_value_list ~values:
      (json
        |> to_assoc
        |> List.map (fun (key, value) -> (
            Common.make_key_value ~key: key ~value: (Some (decode_any_value (value))) ()
            )
          )
      )
    ()
  
  (* Decode an any_value from JSON *)
  and decode_any_value json : Common.any_value =
    match json with
    | `String s when String.length s mod 4 = 0 ->
        (* Example: Check if the string can be decoded as base64 for bytes *)
        (try
          Bytes_value (Base64.decode_exn s |> Bytes.of_string)
        with _ ->
          String_value s)  (* Fallback if base64 decoding fails *)
    | `String s -> String_value s
    | `Bool b -> Bool_value b
    | `Int i -> Int_value (Int64.of_int i)
    | `Float f -> Double_value f
    | `List l -> Array_value (decode_array_value (`List l))
    | `Assoc a -> Kvlist_value (decode_key_value_list (`Assoc a))
    | _ -> failwith "Unsupported JSON value for any_value"


  (* Decode a KeyValue from JSON *)
  let decode_attribute json =
    Common.make_key_value
      ~key: (json |> member "key" |> to_string)
      ~value: (Some (decode_any_value (json |> member "value" |> member "stringValue"))) 
    ()

  let decode_attributes_list json =
    json |> member "attributes" |> to_list |> List.map decode_attribute
  
  let decode_span_kind json : Trace.span_span_kind =
    match json |> member "kind" |> to_string with
        | "SPAN_KIND_INTERNAL" -> Span_kind_internal
        | "SPAN_KIND_SERVER" -> Span_kind_server
        | "SPAN_KIND_CLIENT" -> Span_kind_client
        | "SPAN_KIND_PRODUCER" -> Span_kind_producer
        | "SPAN_KIND_CONSUMER" -> Span_kind_consumer
        | _ -> Span_kind_unspecified

  (* Decode a Span from JSON *)
  let decode_span json : Trace.span =
    Trace.default_span
      ~trace_id: (Bytes.of_string (json |> member "traceId" |> to_string))
      ~span_id: (Bytes.of_string (json |> member "spanId" |> to_string))
      ~parent_span_id: (Bytes.of_string (json |> member "parentSpanId" |> to_string))
      ~name: (json |> member "name" |> to_string)
      ~kind: (decode_span_kind json)
      ~start_time_unix_nano: (Int64.of_int (json |> member "startTimeUnixNano" |> to_int))
      ~end_time_unix_nano: (Int64.of_int (json |> member "endTimeUnixNano" |> to_int))
      ~attributes: (decode_attributes_list json)
      ~status: (Some (Trace.make_status
        ~code: (match json |> member "status" |> to_string with
          | "STATUS_CODE_OK" -> Status_code_ok
          | "STATUS_CODE_ERROR" -> Status_code_error
          | _ -> Status_code_unset)
        ~message: "" (* Optional: Add if status message is needed *)
      ()))
      ~events: []
      ~links: []
      ~dropped_attributes_count: (Int32.of_int 0)
      ~dropped_events_count: (Int32.of_int 0)
      ~dropped_links_count: (Int32.of_int 0) 
    ()
    

  (* Decode an InstrumentationScope from JSON *)
  let decode_scope json =
    Some (Common.make_instrumentation_scope
      ~name: (json |> member "name" |> to_string)
      ~version: "" (* Optional: Add if version is needed *)
      ~attributes: [] (* Optional: Add attributes if needed *)
      ~dropped_attributes_count: (Int32.of_int 0)
    ())

  (* Decode a ScopeSpans from JSON *)
  let decode_scope_spans json =
    Trace.make_scope_spans
      ~scope: (json |> member "scope" |> decode_scope)
      ~spans: (json |> member "spans" |> to_list |> List.map decode_span)
      ~schema_url: "" (* Optional: Add if schema URL is needed *)
    ()

  (* Decode a Resource from JSON *)
  let decode_resource json =
    Some (Resource.make_resource
      ~attributes: (json |> member "attributes" |> to_list |> List.map decode_attribute)
      ~dropped_attributes_count: (Int32.of_int 0) 
    ())

  (* Decode a ResourceSpans from JSON *)
  let decode_resource_spans json =
    Trace.make_resource_spans
      ~resource: (json |> member "resource" |> decode_resource)
      ~scope_spans: (json |> member "scopeSpans" |> to_list |> List.map decode_scope_spans)
      ~schema_url: "" (* Optional: Add if schema URL is needed *)
    ()

  (* Decode the JSON string into a ResourceSpans object *)
  let decode json_string =
    let json = Yojson.Basic.from_string json_string in
    decode_resource_spans (json |> member "resourceSpans")

end
