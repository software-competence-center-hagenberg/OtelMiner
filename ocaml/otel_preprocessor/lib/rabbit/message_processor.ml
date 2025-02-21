open Yojson.Basic.Util
open Amqp_client_async
open Thread
open Util

type result = { trace_id : string; span_trees : Span_tree.span_tree_node list }

(* TODO *)
let decode_and_convert (span : trace_string_type) (data : Yojson.Basic.t) :
    Span_tree.span_tree_node list =
  Log.info "converting data of type %s" (Util.trace_string_type_to_string span);
  Log.info "decoding ...";
  match span with
  | RESOURCE_SPANS ->
      let decoded = Otel_decoder.decode_resource_spans data in
      Log.info "converting ...";
      (* change to tree span*)
      let rec convert_resource_spans_aux l k =
        match l with
        | [] -> k []
        | h :: t ->
            convert_resource_spans_aux t (fun a ->
                k (Span_tree.generate_span_trees_from_resource_spans h :: a))
      in
      List.flatten (convert_resource_spans_aux decoded (fun x -> x))
  | _ ->
      let decoded = Otel_decoder.decode span data in
      Log.info "converting ...";
      Span_tree.generate_span_trees_from_spans decoded

(* TODO *)
let process (span : trace_string_type) (message : string) : result =
  Log.info "processing ...";
  let json = Yojson.Basic.from_string message in
  let trace_id = json |> member "traceId" |> to_string in
  let data = json |> member "spans" in
  Log.info "Trace id: %s" trace_id;
  let span_trees = decode_and_convert span data in
  Log.info "conversion complete";
  Log.info "processing complete";
  { trace_id; span_trees }

let result_to_json_string (result : result) : string =
  let span_trees = Span_tree.list_to_json_string result.span_trees in
  Printf.sprintf "{ \"traceId\": \"%s\", \"spanTrees\": %s }" result.trace_id
    span_trees
