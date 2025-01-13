open Otel_decoder
open Otel_encoder
open OUnit2

let test_decode_trace_string _ =
  let json = Util.load_json_from_file "ac31d6a4e6fab4b650a501274d48d3c5.json" in
  match json with
  | `List lst ->
      let decoded = List.map decode_trace_span lst in
      (*List.iter (pp_span_custom_minimal Format.std_formatter) decoded;*)
      OUnit2.assert_bool "decoding successfull" (not (decoded = []))
  | _ -> OUnit2.assert_failure "Expected a JSON list"

let test_decode_jaeger_trace _ =
  let test_aux json =
    match json with
    | `List lst ->
        let decoded = List.map decode_jaeger_trace_span lst in
        List.iter (pp_span_custom_minimal Format.std_formatter) decoded;
        OUnit2.assert_bool "decoding successfull" true
    | _ -> OUnit2.assert_failure "Expected a JSON list"
  in
  let json = Util.load_json_from_file "jaeger_trace.json" in
  (*let spans = json |> Yojson.Basic.Util.member "data" in*)
  test_aux json;
  let json = Util.load_json_from_file "jaeger_5575e1e883a056898c9ddee917664f9a.json" in
  test_aux json


let suite =
  "Otel_decoder_test_suite"
  >::: [
         "test_decode_trace_string" >:: test_decode_trace_string;
         "test_decode_jaeger_trace" >:: test_decode_jaeger_trace;
       ]

let () = run_test_tt_main suite
