open Yojson.Basic.Util
open Amqp_client_async
open Thread
open Util

type trace_model = { trace_id : string; constraints : Declare.t list }

type traces_model = {
  trace_ids : string list;
  constraints : Declare.t list list;
}

(* TODO *)
let decode_and_convert_resource_spans_or_multiple_traces
    (span : trace_string_type) (data : Yojson.Basic.t) : Declare.t list list =
  Log.info "converting data of type %s" (Util.trace_string_type_to_string span);
  Log.info "decoding ...";
  match span with
  | RESOURCE_SPANS ->
      let decoded = Otel_decoder.decode_resource_spans data in
      Log.info "converting ...";
      Otel_to_prob_declare_converter.convert_resource_spans decoded
  | _ ->
      let decoded = Otel_decoder.decode span data in
      Log.info "converting ...";
      Otel_to_prob_declare_converter.convert_trace_spans_for_multiple_traces
        decoded

(* TODO *)
let decode_and_convert_trace_spans_for_single_trace (span : trace_string_type)
    (data : Yojson.Basic.t) : Declare.t list =
  Log.info "converting data of type %s" (Util.trace_string_type_to_string span);
  Log.info "decoding ...";
  match span with
  | RESOURCE_SPANS ->
      failwith "RESOURECE_SPANS must be handled in other function!"
  | _ ->
      let decoded = Otel_decoder.decode span data in
      Log.info "converting ...";
      Otel_to_prob_declare_converter.convert_trace_spans_for_single_trace
        decoded

(* TODO *)
let process_trace (span : trace_string_type) (message : string) : trace_model =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_id = json |> member "traceId" |> to_string in
  let data = json |> member "spans" in
  Log.info "Trace id: %s" trace_id;
  let constraints = decode_and_convert_trace_spans_for_single_trace span data in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_id; constraints }

let process_traces (span : trace_string_type) (message : string) : traces_model
    =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_ids = json |> member "traceIds" |> to_list |> List.map to_string in
  let data = json |> member "spans" in
  Log.info "Trace ids: %s" (String.concat ", " trace_ids);
  let constraints =
    decode_and_convert_resource_spans_or_multiple_traces span data
  in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_ids; constraints }

let traces_model_to_json_string (model : traces_model) : string =
  let constraints = Declare.list_list_to_json_string model.constraints in
  Printf.sprintf "{ traceId: \"%s\", constraints:\"%s\" }" (Yojson.Basic.pretty_to_string (`List (List.map (fun x -> `String x) model.trace_ids)))
    constraints

let trace_model_to_json_string (model : trace_model) : string =
  let constraints = Declare.list_to_json_string model.constraints in
  Printf.sprintf "{ traceId: \"%s\", constraints:\"%s\" }" model.trace_id
    constraints
