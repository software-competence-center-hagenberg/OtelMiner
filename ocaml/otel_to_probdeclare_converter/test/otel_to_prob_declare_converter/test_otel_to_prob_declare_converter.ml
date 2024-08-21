open OUnit2
open Otel_to_prob_declare_converter
open Util

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
        Declare.ALTERNATE_SUCCESSION ("a", "b");
        Declare.ALTERNATE_SUCCESSION ("a", "c");
        Declare.CHAIN_SUCCESSION ("b", "c");
        Declare.CHAIN_SUCCESSION ("d", "e");
        Declare.PRECEDENCE ("a", "d");
        Declare.PRECEDENCE ("a", "e");
        Declare.SUCCESSION ("b", "d");
        Declare.SUCCESSION ("b", "e");
        Declare.SUCCESSION ("c", "d");
        Declare.SUCCESSION ("c", "e");
      ]
  in
  let result = map_relations activities in
  assert_declare_set_equal expected_relations result

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
         (*
         "test_initialize_conf" >:: test_initialize_conf;
         "test_map_to_declare" >:: test_map_to_declare;
         *)
       ]

let () = run_test_tt_main suite
