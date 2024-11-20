open Yojson.Basic.Util
open Amqp_client_async
open Thread
open Util

type result = { trace_id : string; constraints : Declare.t list list }

(* TODO *)
let decode_and_convert (span : trace_string_type) (data : Yojson.Basic.t) :
    Declare.t list list =
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
      Otel_to_prob_declare_converter.convert_trace_spans decoded

(* TODO *)
let process (span : trace_string_type) (message : string) : result =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_id = json |> member "traceId" |> to_string in
  let data = json |> member "data" in
  Log.info "Trace id: %s" trace_id;
  let constraints = decode_and_convert span data in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_id; constraints }

let result_to_json_string (result : result) : string =
  let constraints = Declare.to_json_string result.constraints in
  Printf.sprintf "{ traceId: \"%s\", constraints:\"%s\" }" result.trace_id
    constraints
