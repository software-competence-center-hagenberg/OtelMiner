open Ltl

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
