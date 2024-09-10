open OUnit2
open Otel_to_prob_declare_converter
open Util
open Otel_decoder

let assert_declare_set_equal ds1 ds2 =
  assert_equal ~cmp:DeclareSet.equal
    ~printer:(fun ds ->
      String.concat ", " (List.map Declare.to_string (DeclareSet.elements ds)))
    ds1 ds2

let test_determine_relation _ =
  let activities = [ "a"; "b"; "c" ] in
  assert_equal
    (Some (Declare.CHAIN_SUCCESSION ("a", "b")))
    (determine_relation "a" "b" activities);
  assert_equal
    (Some (Declare.SUCCESSION ("a", "c")))
    (determine_relation "a" "c" activities);
  assert_equal None (determine_relation "b" "a" activities)

let test_map_relations _ =
  let activities = [ "a"; "b"; "c"; "a"; "d"; "e" ] in
  let expected_relations =
    DeclareSet.of_list
      [
        ALTERNATE_SUCCESSION ("a", "b");
        ALTERNATE_SUCCESSION ("a", "c");
        CHAIN_SUCCESSION ("b", "c");
        CHAIN_SUCCESSION ("d", "e");
        PRECEDENCE ("a", "d");
        PRECEDENCE ("a", "e");
        SUCCESSION ("b", "d");
        SUCCESSION ("b", "e");
        SUCCESSION ("c", "d");
        SUCCESSION ("c", "e");
      ]
  in
  let result = map_relations activities in
  assert_declare_set_equal expected_relations result

let ac31d6a4e6fab4b650a501274d48d3c5_model =
  DeclareSet.of_list
    [ EXISTENCE "GET /travel/adminQueryAll"; INIT "GET /travel/adminQueryAll" ]

let jaeger_trace_model =
  DeclareSet.of_list
    [
      CHAIN_SUCCESSION
        ("POST /travel/getTripsByRouteId", "TravelController.getTripsByRouteId");
      CHAIN_SUCCESSION
        ("TravelController.getTripsByRouteId", "TripRepository.findByRouteId");
      CHAIN_SUCCESSION ("TripRepository.findByRouteId", "find ts.trip");
      EXISTENCE "POST /travel/getTripsByRouteId";
      EXISTENCE "TravelController.getTripsByRouteId";
      EXISTENCE "TripRepository.findByRouteId";
      EXISTENCE "find ts.trip";
      INIT "POST /travel/getTripsByRouteId";
      LAST "find ts.trip";
      SUCCESSION
        ("POST /travel/getTripsByRouteId", "TripRepository.findByRouteId");
      SUCCESSION ("POST /travel/getTripsByRouteId", "find ts.trip");
      SUCCESSION ("TravelController.getTripsByRouteId", "find ts.trip");
    ]

let test_convert_trace_spans _ =
  let json =
    Util.load_json_from_file ~prefix:"/../../../../test/"
      "ac31d6a4e6fab4b650a501274d48d3c5.json"
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let declare = convert_trace_spans decoded in
  (*Format.print_string (Declare.string_of_declare_list_list declare);*)
  assert_equal ac31d6a4e6fab4b650a501274d48d3c5_model
    (DeclareSet.of_list (List.flatten declare));
  let json =
    Util.load_json_from_file ~prefix:"/../../../../test/" "jaeger_trace.json"
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let declare = convert_trace_spans decoded in
  (*Format.print_string (Declare.string_of_declare_list_list declare);*)
  assert_equal jaeger_trace_model (DeclareSet.of_list (List.flatten declare))

(*
let test_initialize_conf _ =
  let root = mock_span_tree_node "root" [] in
  let conf = initialize_conf root in
  assert_equal [ "root" ] conf.p;
  assert_bool "Activity set should contain root" (StringSet.mem "root" conf.a);
  assert_bool "Constraints should contain Declare.INIT"
    (DeclareSet.mem (Declare.INIT "root") conf.c)
let test_map_to_declare _ =
  let root =
    mock_span_tree_node "root"
      [ mock_span_tree_node "a" []; mock_span_tree_node "b" [] ]
  in
  let result = map_to_declare root in
  let expected =
    [
      Declare.INIT "root";
      Declare.CHOICE ("root", "a");
      Declare.CHOICE ("root", "b");
      Declare.LAST "a";
      Declare.LAST "b";
      Declare.EXISTENCE "root";
      Declare.EXISTENCE "a";
      Declare.EXISTENCE "b";
    ]
  in
  assert_equal expected result
*)

let suite =
  "OtelToProbDeclareConverterTestSuite"
  >::: [
         "test_determine_relation" >:: test_determine_relation;
         "test_map_relations" >:: test_map_relations;
         "test_convert_trace_spans" >:: test_convert_trace_spans;
         (*
         "test_initialize_conf" >:: test_initialize_conf;
         "test_map_to_declare" >:: test_map_to_declare;
         *)
       ]

let () = run_test_tt_main suite
