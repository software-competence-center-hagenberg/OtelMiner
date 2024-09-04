open Otel_decoder
open OUnit2
open Span_tree

let test_generate_span_trees_from_spans _ =
  let json =
    Yojson.Basic.from_file
      (Sys.getcwd ()
     ^ "/../../../../../test/ac31d6a4e6fab4b650a501274d48d3c5.json")
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let span_trees = generate_span_trees_from_spans decoded in
  List.iter (pp_span_tree Format.std_formatter) span_trees;
  assert_bool "" true

let test_generate_nodes _ =
  let json =
    Yojson.Basic.from_file
      (Sys.getcwd ()
     ^ "/../../../../../test/ac31d6a4e6fab4b650a501274d48d3c5.json")
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let roots, nodes = generate_nodes decoded in
  Format.fprintf Format.std_formatter "#####################################\n";
  Format.fprintf Format.std_formatter "{\ntest_generate_nodes:\n";
  Format.fprintf Format.std_formatter "roots:\n\n[";
  List.iter (pp_span_tree Format.std_formatter) roots;
  Format.fprintf Format.std_formatter "\n\n]\nnodes:\n[";
  List.iter (pp_span_tree Format.std_formatter) nodes;
  Format.fprintf Format.std_formatter
    "\n]\n}\n#####################################\n";
  assert_bool "" true

let suite =
  "span_tree_test_suite"
  >::: [
         "test_generate_span_trees_from_spans"
         >:: test_generate_span_trees_from_spans;
         "test_generate_nodes" >:: test_generate_nodes;
       ]

let () = run_test_tt_main suite
