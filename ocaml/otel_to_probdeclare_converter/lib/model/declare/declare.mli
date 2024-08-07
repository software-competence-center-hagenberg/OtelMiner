type t =
  (* existence *)
  | EXISTENCE of string
  | ABSENCE of string
  | AT_LEAST of string * int
  | AT_MOST of string * int
  | EXACTLY of string * int
  | INIT of string
  | LAST of string
  (* relation *)
  | RESPONDED_EXISTENCE of string * string
  | CO_EXISTENCE of string * string
  | RESPONSE of string * string
  | ALTERNATE_RESPONSE of string * string
  | CHAIN_RESPONSE of string * string
  | PRECEDENCE of string * string
  | ALTERNATE_PRECEDENCE of string * string
  | CHAIN_PRECEDENCE of string * string
  | SUCCESSION of string * string
  | ALTERNATE_SUCCESSION of string * string
  | CHAIN_SUCCESSION of string * string
  (* negation *)
  | NOT_RESPONDED_EXISTENCE of string * string
  | NOT_RESPONSE of string * string
  | NOT_CHAIN_RESPONSE of string * string
  | NOT_PRECEDENCE of string * string
  | NOT_CHAIN_PRECEDENCE of string * string
  | NOT_COEXISTENCE of string * string
  (* choice *)
  | CHOICE of string * string
  | EXCLUSIVE_CHOICE of string * string

val map_declare_to_ltl : t -> Ltl.term
val compare : t -> t -> int
