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

let m2 =
  DeclareSet.of_list
    [
      CHAIN_SUCCESSION ("POST /travel/create", "BasicErrorController.error");
      CHAIN_SUCCESSION ("POST /travel/create", "TravelController.create");
      CHAIN_SUCCESSION ("TravelController.create", "TripRepository.findByTripId");
      CHAIN_SUCCESSION ("TravelController.create", "TripRepository.save");
      CHAIN_SUCCESSION ("TripRepository.findByTripId", "find ts.trip");
      CHAIN_SUCCESSION ("TripRepository.save", "update ts.trip");
      CHOICE ("TravelController.create", "BasicErrorController.error");
      CHOICE ("TripRepository.findByTripId", "TripRepository.save");
      EXISTENCE "BasicErrorController.error";
      EXISTENCE "POST /travel/create";
      EXISTENCE "TravelController.create";
      EXISTENCE "TripRepository.findByTripId";
      EXISTENCE "TripRepository.save";
      EXISTENCE "find ts.trip";
      EXISTENCE "update ts.trip";
      INIT "POST /travel/create";
      LAST "BasicErrorController.error";
      LAST "find ts.trip";
      LAST "update ts.trip";
      SUCCESSION ("POST /travel/create", "TripRepository.findByTripId");
      SUCCESSION ("POST /travel/create", "TripRepository.save");
      SUCCESSION ("POST /travel/create", "find ts.trip");
      SUCCESSION ("POST /travel/create", "update ts.trip");
      SUCCESSION ("TravelController.create", "find ts.trip");
      SUCCESSION ("TravelController.create", "update ts.trip");
    ]

let test_convert_trace_spans _ =
  let test_aux file_name expected_model decoding_function conversion_function =
    let json =
      Util.load_json_from_file ~prefix:"/../../../../test/" file_name
    in
    let spans = json |> Yojson.Basic.Util.to_list in
    let decoded = decoding_function spans in
    let declare = conversion_function decoded in
    let printer set =
      let elements = DeclareSet.elements set in
      "[" ^ String.concat "; " (List.map Declare.to_string elements) ^ "]"
    in
    assert_equal ~printer expected_model
      (DeclareSet.of_list (List.flatten declare))
  in
  test_aux "ac31d6a4e6fab4b650a501274d48d3c5.json"
    ac31d6a4e6fab4b650a501274d48d3c5_model
    (fun x -> List.map decode_trace_span x)
    (fun x -> convert_trace_spans x);
  test_aux "jaeger_trace.json" jaeger_trace_model
    (fun x -> List.map decode_jaeger_trace_span x)
    (fun x -> convert_trace_spans x);
  test_aux "jaeger_5575e1e883a056898c9ddee917664f9a.json" m2
    (fun x -> List.map decode_jaeger_trace_span x)
    (fun x -> convert_trace_spans x)

let test_map_choices _ =
  let printer set =
    let elements = DeclareSet.elements set in
    "[" ^ String.concat "; " (List.map Declare.to_string elements) ^ "]"
  in
  let test_aux expected actual = 
    (* FIXME find out why sets need to be sorted that way in order to evaluate correctly *)
    let e = List.sort Declare.compare (DeclareSet.elements expected) in
    let a = List.sort Declare.compare (DeclareSet.elements actual) in
    assert_equal ~printer (DeclareSet.of_list e) (DeclareSet.of_list a) in
  test_aux (DeclareSet.of_list [ CHOICE ("a", "b") ]) (map_choices [ "a"; "b" ]);
  test_aux
    (DeclareSet.of_list
       [ CHOICE ("a", "b"); CHOICE ("a", "c"); CHOICE ("b", "c") ])
    (map_choices [ "a"; "b"; "c" ]);
  test_aux
    (DeclareSet.of_list
       [
         CHOICE ("a", "b");
         CHOICE ("a", "c");
         CHOICE ("a", "d");
         CHOICE ("b", "c");
         CHOICE ("b", "d");
         CHOICE ("c", "d");
       ])
    (map_choices [ "a"; "b"; "c"; "d" ])

let suite =
  "OtelToProbDeclareConverterTestSuite"
  >::: [
         "test_determine_relation" >:: test_determine_relation;
         "test_map_relations" >:: test_map_relations;
         "test_convert_trace_spans" >:: test_convert_trace_spans;
         "test_map_choices" >:: test_map_choices;
         (*
         "test_initialize_conf" >:: test_initialize_conf;
         "test_map_to_declare" >:: test_map_to_declare;
         *)
       ]

let () = run_test_tt_main suite
