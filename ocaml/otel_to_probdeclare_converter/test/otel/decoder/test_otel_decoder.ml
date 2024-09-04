open Otel_decoder
open Otel_encoder
open OUnit2

let test_decode_trace_string _ =
  let json =
    Yojson.Basic.from_file
      (Sys.getcwd ()
     ^ "/../../../../../test/ac31d6a4e6fab4b650a501274d48d3c5.json")
  in
  match json with
  | `List lst ->
      let decoded = List.map decode_trace_span lst in
      List.iter (pp_span_custom_minimal Format.std_formatter) decoded;
      OUnit2.assert_bool "decoding successfull" true
  | _ -> OUnit2.assert_failure "Expected a JSON list"

let suite =
  "Otel_decoder_test_suite"
  >::: [ "test_decode_trace_string" >:: test_decode_trace_string ]

let () = run_test_tt_main suite
