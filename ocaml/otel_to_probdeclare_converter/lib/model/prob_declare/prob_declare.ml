open Ltl

(*
 * -----------------------------------------------------------------------------
 * TODO: eval if ltl better than string!
 * -----------------------------------------------------------------------------
 * Definition taken from Table 1 of N. Schützemmeier et al. "Upper-Bounded 
 * Model Checking for Declarative Process Models"
 * -----------------------------------------------------------------------------
 * existence(A) = F(A)
 * absence(A) = ¬F(A)
 * atLeast(A, n) = F(A ∧ X(atLeast(A, n − 1))), atLeast(A, 1) = F(A)
 * atMost(A, n) = G(¬A ∨ X(atMost(A, n − 1))), atMost(A, 0) = G(¬A)
 * init(A) = A
 * last(A) = G(¬A → F(A))
 * respondedExistence(A, B) = F(A) → F(B)
 * response(A, B) = G(A → F(B))
 * alternateResponse(A, B) = G(A → X(¬AUB))
 * chainResponse(A, B) = G(A → X(B)) ∧ response(A, B)
 * precedence(A, B) = F(B) → ((¬B)UA)
 * alternatePrecedence(A, B) = precedence(A, B) ∧ G(B → X(precedence(A, B))
 * chainPrecedence(A, B) = precedence(A, B) ∧ G(X(B) → A)
 * succession(A, B) = response(A, B) ∧ precedence(A, B)
 * chainSuccession(A, B) = G(A ↔ X(B))
 * alternateSuccession(A, B) = alternateResponse(A, B) 
 *                               ∧ alternatePrecedence(A, B)
 * notRespondedExistence(A, B) =  F(A) → F(B)
 * notResponse(A, B) = G(A → ¬F(B))
 * notPrecedence(A, B) = G(F (B) → ¬A)
 * notChainResponse(A, B) = G(A → ¬X(B))
 * notChainPrecedence(A, B) = G(X(B) → ¬A)
 * coExistence(A, B) = F(A) ↔ F(B)
 * notCoExistence(A, B) = ¬(F(A) ∧ F(B))
 * choice(A, B) = F(A) ∨ F(B)
 * exclusiveChoice(A, B) = (F(A) ∨ F(B)) ∧ ¬(F(A) ∧ F(B))
 * -----------------------------------------------------------------------------
 * addition: exactly(A, n) = atLeast(A,n) ∧ atMost(A,n)
 * -----------------------------------------------------------------------------
 *)
type declare_constraint =
  (* existence *)
  | EXISTENCE of term
  | AT_LEAST of term * int
  | AT_MOST of term * int
  | EXACTLY of term * int
  | INIT of term
  | LAST of term
  (* relation *)
  | RESPONDED_EXISTENCE of term * term
  | RESPONSE of term * term
  | ALTERNATE_RESPONSE of term * term
  | CHAIN_RESPONSE of term * term
  | PRECEDENCE of term * term
  | ALTERNATE_PRECEDENCE of term * term
  | CHAIN_PRECEDENCE of term * term
  | SUCCESSION of term * term
  | ALTERNATE_SUCCESSION of term * term
  | CHAIN_SUCCESSION of term * term
  | CO_EXISTENCE of term * term
  (* negation *)
  | NOT_RESPONDED_EXISTENCE of term * term
  | NOT_RESPONSE of term * term
  | NOT_CHAIN_RESPONSE of term * term
  | NOT_PRECEDENCE of term * term
  | NOT_CHAIN_PRECEDENCE of term * term
  | NOT_COEXISTENCE of term * term
  (* choice *)
  | CHOICE of term * term
  | EXCLUSIVE_CHOICE of term * term

type pd_val = { declare : declare_constraint; ltl : term }

type prob_declare =
  | PROB_DECLARE of {
      crisps : pd_val list;
      probabilities : float * pd_val list;
    }

let map_declare_to_ltl d =
  match d with
  | EXISTENCE a -> Ltl.existence a
  | AT_LEAST (a, n) -> Ltl.at_most a n
  | AT_MOST (a, n) -> Ltl.at_most a n
  | EXACTLY (a, n) -> Ltl.exactly a n
  | INIT a -> Ltl.init a
  | LAST a -> Ltl.last a
  | RESPONDED_EXISTENCE (a, b) -> Ltl.responded_existence a b
  | RESPONSE (a, b) -> Ltl.response a b
  | ALTERNATE_RESPONSE (a, b) -> Ltl.alternate_response a b
  | CHAIN_RESPONSE (a, b) -> Ltl.chain_response a b
  | PRECEDENCE (a, b) -> Ltl.precedence a b
  | ALTERNATE_PRECEDENCE (a, b) -> Ltl.alternate_precedence a b
  | CHAIN_PRECEDENCE (a, b) -> Ltl.chain_precedence a b
  | SUCCESSION (a, b) -> Ltl.succession a b
  | ALTERNATE_SUCCESSION (a, b) -> Ltl.alternate_succession a b
  | CHAIN_SUCCESSION (a, b) -> Ltl.chain_succession a b
  | CO_EXISTENCE (a, b) -> Ltl.co_existence a b
  | NOT_RESPONDED_EXISTENCE (a, b) -> Ltl.not_responded_existence a b
  | NOT_RESPONSE (a, b) -> Ltl.not_response a b
  | NOT_CHAIN_RESPONSE (a, b) -> Ltl.not_chain_response a b
  | NOT_PRECEDENCE (a, b) -> Ltl.not_precedence a b
  | NOT_CHAIN_PRECEDENCE (a, b) -> Ltl.not_chain_precedence a b
  | NOT_COEXISTENCE (a, b) -> Ltl.not_co_existence a b
  | CHOICE (a, b) -> Ltl.choice a b
  | EXCLUSIVE_CHOICE (a, b) -> Ltl.exclusive_choice a b
