open Ltl

(*
 * exactly(a,n)         Activity a must occur exactly n times
 * existence(a,n)       a must occur at least n times
 * max(a,n)             a must occur at most n times
 * init(a)              a must be the first executed activity in every trace
 * last(a)              a must be the last executed activity in every trace
 * precedence(a,b)      activity b must be preceded by activity a   
 *                      (not necessarily directly)
 * response(a,b)        If a is executed, b must be executed afterward (not
 *                      necessarily directly afterward)
 * succession(a,b)      Combines precedence(a,b) and response(a,b)
 * chain_response(a,b)  If a is executed, b is executed directly afterward
 * coexistence(a,b)     If a is executed, b must be executed and vice versa
 * neg_response(a,b)    If a is executed, b must not be executed afterward
 * neg_coexistence(a,b) a and b cannot co-occur in any trace
 *)
type declare =
  (* existence *)
  | EXACTLY of string * int
  | EXISTENCE of string * int
  | MAX of string * int
  | INIT of string
  | LAST of string
  (* relation *)
  | RESPONDED_EXISTENCE of string * string
  | RESPONSE of string * string
  | ALTERNATE_RESPONSE of string *string
  | CHAIN_RESPONSE of string * string
  | PRECEDENCE of string * string
  | ALTERNATE_PRECEDENCE of string *string
  | CHAIN_PRECEDENCE of string * string
  | SUCCESSION of string * string
  | ALTERNATE_SUCCESSION of string *string
  | CHAIN_SUCCESSION of string * string
  | COEXISTENCE of string * string
  (* negation *)
  | NOT_RESPONDED_EXISTENCE of string * string
  | NOT_RESPONSE of string * string
  | NOT_CHAIN_RESPONSE of string * string
  | NOT_PRECEDENCE of string * string
  | NOT_CHAIN_PRECEDENCE of string * string
  | NOT_EXISTENCE of string * string
  | NOT_COEXISTENCE of string * string
  (* choice *)
  | CHOICE of string * string
  | EXCLUSIVE_CHOICE of string * string

type term = D of declare | L of ltl

type prob_declare =
  | PROB_DECLARE of { crisps : term list; probabilities : float * term list }

