type term =
  | V of string (* Represents a variable or proposition *)
  | NOT of term (* ¬ *)
  | X of term (* Next operator *)
  | F of term (* Eventually operator *)
  | G of term (* Globally operator *)
  | U of term * term (* Until operator *)
  | THEN of term * term (* → *)
  | IFF of term * term (* ↔ *)
  | AND of term * term (* ∧ *)
  | OR of term * term (* ∨ *)

(* Function signatures for LTL operations *)
val existence : term -> term
val absence : term -> term
val at_least : term -> int -> term
val at_most : term -> int -> term
val exactly : term -> int -> term
val init : term -> term
val last : term -> term
val responded_existence : term -> term -> term
val response : term -> term -> term
val alternate_response : term -> term -> term
val chain_response : term -> term -> term
val precedence : term -> term -> term
val alternate_precedence : term -> term -> term
val chain_precedence : term -> term -> term
val succession : term -> term -> term
val chain_succession : term -> term -> term
val alternate_succession : term -> term -> term
val not_responded_existence : term -> term -> term
val not_response : term -> term -> term
val not_precedence : term -> term -> term
val not_chain_response : term -> term -> term
val not_chain_precedence : term -> term -> term
val co_existence : term -> term -> term
val not_co_existence : term -> term -> term
val choice : term -> term -> term
val exclusive_choice : term -> term -> term

(* Utility functions for printing LTL formulas *)
val string_of_ltl : term -> string
val string_of_ltl_list : term list -> string
(*val print_ltls : unit*)
val compare : term -> term -> int
