open Yojson.Basic.Util
open Amqp_client_async
open Thread
open Util

type trace_model = { trace_id : string; constraints : Declare.t list }

type traces_model = {
  trace_ids : string list;
  constraints : Declare.t list list;
}

let decode_and_convert_resource_spans_or_multiple_traces (tt : trace_type)
    (data : Yojson.Basic.t) : Declare.t list list =
  Log.info "converting data of type %s" (Util.trace_string_type_to_string tt);
  Log.info "decoding ...";
  match tt with
  | RESOURCE_SPANS ->
      let decoded = Otel_decoder.decode_resource_spans data in
      Log.info "converting ...";
      Otel_to_prob_declare_converter.convert_resource_spans decoded
  | _ ->
      let decoded = Otel_decoder.decode tt data in
      Log.info "converting ...";
      Otel_to_prob_declare_converter.convert_trace_spans_for_multiple_traces
        decoded

let decode_and_convert_trace_spans_for_single_trace (tt : trace_type)
    (data : Yojson.Basic.t) : Declare.t list =
  Log.info "decoding and converting data of type %s"
    (Util.trace_string_type_to_string tt);
  Log.info "decoding ...";
  let decoded = Otel_decoder.decode tt data in
  Log.info "converting ...";
  match tt with
  | RESOURCE_SPANS ->
      failwith "RESOURECE_SPANS must be handled in other function!"
  | DYNATRACE_SPANS_LIST ->
      Otel_to_prob_declare_converter
      .convert_trace_spans_for_single_trace_without_parent_span_ids decoded
  | _ ->
      Otel_to_prob_declare_converter.convert_trace_spans_for_single_trace
        decoded

let process_trace (tt : trace_type) (message : string) : trace_model =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_id = json |> member "traceId" |> to_string in
  let data = json |> member "spans" in
  Log.info "Trace id: %s" trace_id;
  let constraints = decode_and_convert_trace_spans_for_single_trace tt data in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_id; constraints }

let process_traces (tt : trace_type) (message : string) : traces_model =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_ids = json |> member "traceIds" |> to_list |> List.map to_string in
  let data = json |> member "spans" in
  Log.info "Trace ids: %s" (String.concat ", " trace_ids);
  let constraints =
    decode_and_convert_resource_spans_or_multiple_traces tt data
  in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_ids; constraints }

let traces_model_to_json_string (model : traces_model) : string =
  let constraints =
    Declare.list_list_to_json_string_list_list model.constraints
  in
  let json_obj =
    `Assoc
      [
        ("traceIds", `List (List.map (fun id -> `String id) model.trace_ids));
        ("constraints", constraints);
      ]
  in
  Yojson.Basic.pretty_to_string json_obj

let trace_model_to_json_string (model : trace_model) : string =
  let constraints = Declare.list_to_json_string_list model.constraints in
  let json_obj =
    `Assoc [ ("traceId", `String model.trace_id); ("constraints", constraints) ]
  in
  Yojson.Basic.pretty_to_string json_obj
