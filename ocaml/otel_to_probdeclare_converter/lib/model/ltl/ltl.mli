type ltl =
  | V of string (* Represents a variable or proposition *)
  | G of ltl (* Globally operator *)
  | F of ltl (* Eventually operator *)
  | X of ltl (* Next operator *)
  | NOT of ltl (* Negation operator *)
  | U of ltl * ltl (* Until operator *)
  | THEN of ltl * ltl (* Implication operator *)
  | IFF of ltl * ltl (* If and only if operator *)
  | AND of ltl * ltl (* Logical AND operator *)
  | OR of ltl * ltl (* Logical OR operator *)

(* Function signatures for LTL operations *)
val existence : ltl -> ltl
val absence : ltl -> ltl
val at_least : ltl -> int -> ltl
val at_most : ltl -> int -> ltl
val init : ltl -> ltl
val last : ltl -> ltl
val responded_existence : ltl -> ltl -> ltl
val response : ltl -> ltl -> ltl
val alternate_response : ltl -> ltl -> ltl
val chain_response : ltl -> ltl -> ltl
val precedence : ltl -> ltl -> ltl
val alternate_precedence : ltl -> ltl -> ltl
val chain_precedence : ltl -> ltl -> ltl
val succession : ltl -> ltl -> ltl
val chain_succession : ltl -> ltl -> ltl
val alternate_succession : ltl -> ltl -> ltl
val not_responded_existence : ltl -> ltl -> ltl
val not_response : ltl -> ltl -> ltl
val not_precedence : ltl -> ltl -> ltl
val not_chain_response : ltl -> ltl -> ltl
val not_chain_precedence : ltl -> ltl -> ltl
val co_existence : ltl -> ltl -> ltl
val not_co_existence : ltl -> ltl -> ltl
val choice : ltl -> ltl -> ltl
val exclusive_choice : ltl -> ltl -> ltl

(* Utility functions for printing LTL formulas *)
val string_of_ltl : ltl -> string
val string_of_ltl_list : ltl list -> string
val print_ltls : unit
