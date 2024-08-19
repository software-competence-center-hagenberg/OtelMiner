open Activity
open OUnit2

let test_find_next _ =
  assert_equal [ "b"; "c" ] (find_next "b" [ "a"; "b"; "c" ]);
  assert_equal [] (find_next "d" [ "a"; "b"; "c" ])

let test_find_next_a_or_b _ =
  assert_equal [ "a"; "b"; "c"; "d" ]
    (find_next_a_or_b "a" "b" [ "a"; "b"; "c"; "d" ]);
  assert_equal [ "b"; "a" ] (find_next_a_or_b "a" "b" [ "d"; "e"; "b"; "a" ])

let test_is_relation _ =
  assert_bool "is relation should be false"
    (not
       (is_relation "a" "b" [ "a"; "b"; "c"; "d" ]
          (fun _ _ _ -> true)
          (fun _ -> [])
          (fun _ _ _ -> false)
          (fun _ -> false)));
  assert_bool "is relation should be false"
    (not
       (is_relation "a" "b" [ "a"; "b"; "c"; "d" ]
          (fun _ _ _ -> false)
          (fun _ -> [])
          (fun _ _ _ -> false)
          (fun _ -> false)));
  assert_bool "is relation should be false"
    (not
       (is_relation "a" "b" [ "a"; "b"; "c"; "d" ]
          (fun _ _ _ -> false)
          (fun _ -> [])
          (fun _ _ _ -> true)
          (fun _ -> false)));
  assert_bool "is relation should be true"
    (is_relation "a" "b" [ "a"; "b"; "c"; "d" ]
       (fun _ _ _ -> false)
       (fun _ -> [])
       (fun _ _ _ -> false)
       (fun _ -> true));
  assert_bool "is relation should be true"
    (is_relation "a" "b" [ "a"; "b"; "c"; "d" ]
       (fun _ _ _ -> false)
       (fun _ -> [])
       (fun _ _ _ -> true)
       (fun _ -> true))

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

let test_is_alternate_succession _ =
  assert_bool "Alternate succession should be true"
    (is_alternate_succession "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Alternate succession should be false"
    (not (is_alternate_succession "a" "b" [ "a"; "c"; "b" ]))

let test_is_alternate_response _ =
  assert_bool "Alternate response should be true"
    (is_alternate_response "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Alternate response should be false"
    (not (is_alternate_response "a" "b" [ "a"; "c"; "b" ]))

let test_is_alternate_precedence _ =
  assert_bool "Alternate precedence should be true"
    (is_alternate_precedence "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Alternate precedence should be false"
    (not (is_alternate_precedence "a" "b" [ "a"; "c"; "b" ]))

let test_is_succession _ =
  assert_bool "Succession should be true"
    (is_succession "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Succession should be true"
    (is_succession "a" "b" [ "a"; "c"; "b" ]);
  assert_bool "Succession should be false"
    (not (is_succession "a" "b" [ "b"; "c"; "a" ]));
  assert_bool "Succession should be false"
    (not (is_succession "a" "b" [ "d"; "c"; "a" ]))

let test_is_response _ =
  assert_bool "Response should be true"
    (is_response "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Response should be true" (is_response "a" "b" [ "a"; "c"; "b" ]);
  assert_bool "Response should be false"
    (not (is_response "a" "b" [ "a"; "c"; "b" ]))

let test_is_precedence _ =
  assert_bool "Precedence should be true"
    (is_precedence "a" "b" [ "a"; "c"; "b"; "d"; "a" ]);
  assert_bool "Precedence should be false"
    (not (is_precedence "a" "b" [ "a"; "c"; "b" ]));
  assert_bool "Precedence should be false"
    (not (is_precedence "a" "b" [ "b"; "c"; "a" ]));
  assert_bool "Precedence should be false"
    (not (is_precedence "a" "b" [ "d"; "c"; "a" ]))

let suite =
  "Activity_test_suite"
  >::: [
         "test_find_next" >:: test_find_next;
         "test_find_next_a_or_b" >:: test_find_next_a_or_b;
         "test_is_relation" >:: test_is_relation;
         "test_is_chain_succession" >:: test_is_chain_succession;
         "test_is_chain_response" >:: test_is_chain_response;
         "test_is_chain_precedence" >:: test_is_chain_precedence;
         "test_is_alternate_succession" >:: test_is_alternate_succession;
         "test_is_alternate_response" >:: test_is_alternate_response;
         "test_is_alternate_precedence" >:: test_is_alternate_precedence;
         "test_is_succession" >:: test_is_succession;
         "test_is_response" >:: test_is_response;
         "test_is_precedence" >:: test_is_precedence;
       ]

let () = run_test_tt_main suite
