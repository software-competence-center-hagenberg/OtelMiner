open Activity
open OUnit2

let test_find_next _ =
  assert_equal [ "b"; "c" ] (find_next "b" [ "a"; "b"; "c" ]);
  assert_equal [] (find_next "d" [ "a"; "b"; "c" ])

let test_find_next_a_or_b _ =
  assert_equal [ "b"; "c" ] (find_next_a_or_b "b" "d" [ "a"; "b"; "c" ]);
  assert_equal [ "d"; "e" ] (find_next_a_or_b "b" "d" [ "a"; "d"; "e" ])

let test_is_chain_succession _ =
  assert_bool "Chain succession should be true"
    (is_chain_succession "a" "b" [ "a"; "b"; "c" ]);
  assert_bool "Chain succession should be false"
    (not (is_chain_succession "a" "b" [ "c"; "b"; "a" ]))

let test_is_chain_response _ =
  assert_bool "Chain response should be true"
    (is_chain_response "a" "b" [ "a"; "b"; "c" ]);
  assert_bool "Chain response should be false"
    (not (is_chain_response "a" "b" [ "c"; "a"; "d"; "b" ]))

let test_is_chain_precedence _ =
  assert_bool "Chain precedence should be true"
    (is_chain_precedence "a" "b" [ "a"; "b"; "c" ]);
  assert_bool "Chain precedence should be false"
    (not (is_chain_precedence "a" "b" [ "a"; "c"; "b" ]))

let suite =
  "Activity_test_suite"
  >::: [
         "test_find_next" >:: test_find_next;
         "test_find_next_a_or_b" >:: test_find_next_a_or_b;
         "test_is_chain_succession" >:: test_is_chain_succession;
         "test_is_chain_response" >:: test_is_chain_response;
         "test_is_chain_precedence" >:: test_is_chain_precedence;
       ]

let () = run_test_tt_main suite
