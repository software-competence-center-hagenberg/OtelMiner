type declare =
  (* existence *)
  | EXISTENCE of Ltl.term
  | AT_LEAST of Ltl.term * int
  | AT_MOST of Ltl.term * int
  | EXACTLY of Ltl.term * int
  | INIT of Ltl.term
  | LAST of Ltl.term
  (* relation *)
  | RESPONDED_EXISTENCE of Ltl.term * Ltl.term
  | RESPONSE of Ltl.term * Ltl.term
  | ALTERNATE_RESPONSE of Ltl.term * Ltl.term
  | CHAIN_RESPONSE of Ltl.term * Ltl.term
  | PRECEDENCE of Ltl.term * Ltl.term
  | ALTERNATE_PRECEDENCE of Ltl.term * Ltl.term
  | CHAIN_PRECEDENCE of Ltl.term * Ltl.term
  | SUCCESSION of Ltl.term * Ltl.term
  | ALTERNATE_SUCCESSION of Ltl.term * Ltl.term
  | CHAIN_SUCCESSION of Ltl.term * Ltl.term
  | CO_EXISTENCE of Ltl.term * Ltl.term
  (* negation *)
  | NOT_RESPONDED_EXISTENCE of Ltl.term * Ltl.term
  | NOT_RESPONSE of Ltl.term * Ltl.term
  | NOT_CHAIN_RESPONSE of Ltl.term * Ltl.term
  | NOT_PRECEDENCE of Ltl.term * Ltl.term
  | NOT_CHAIN_PRECEDENCE of Ltl.term * Ltl.term
  | NOT_COEXISTENCE of Ltl.term * Ltl.term
  (* choice *)
  | CHOICE of Ltl.term * Ltl.term
  | EXCLUSIVE_CHOICE of Ltl.term * Ltl.term

type pd_val = { declare : declare; ltl : Ltl.term }

type prob_declare =
  | PROB_DECLARE of {
      crisps : pd_val list;
      probabilities : float * pd_val list;
    }
