(* 
 * -----------------------------------------------------------------------------
 * TODO eval if differentiation between variable and term is needed!
 * -----------------------------------------------------------------------------
 * Note that there is no weak until -> instead use OR (U(a,b), G(V (a)))
 * BLACK has next operator and X ... FIXME check if X is ok
 * -----------------------------------------------------------------------------
 *)
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

(* existence(A) = F(A) *)
let existence a = F a

(* absence(A) = ¬F(A) *)
let absence a = NOT (existence a)

(* atLeast(A, n) = F(A ∧ X(atLeast(A, n − 1))), atLeast(A, 1) = F(A) *)
let rec at_least a n =
  match n with
  | 1 -> F a
  | _ when n < 1 -> failwith "at_least n must not be < 1!"
  | _ -> F (AND (a, X (at_least a (n - 1))))

(* atMost(A, n) = G(¬A ∨ X(atMost(A, n − 1))), atMost(A, 0) = G(¬A) *)
let rec at_most a n =
  match n with
  | 0 -> G (NOT a)
  | _ when n < 0 -> failwith "at_most n must not be < 0!"
  | _ -> G (OR (NOT a, X (at_most a (n - 1))))

(* exactly(A, n) = atLeast(A, n) ∧ atMost(A, n) *)
let exactly a n = AND (at_least a n, at_most a n)

(* init(A) = A *)
let init a = a

(* last(A) = G(¬A → F(A)) *)
let last a = G (THEN (NOT a, F a))

(* respondedExistence(A, B) = F(A) → F(B) *)
let responded_existence a b = THEN (F a, F b)

(* coExistence(A, B) = F(A) ↔ F(B) *)
let co_existence a b = IFF (F a, F b)

(* response(A, B) = G(A → F(B)) *)
let response a b = G (THEN (a, F b))

(* alternateResponse(A, B) = G(A → X(¬A U B)) *)
let alternate_response a b = G (THEN (a, X (U (NOT a, b))))

(* chainResponse(A, B) = G(A → X(B)) ∧ response(A, B) *)
let chain_response a b = AND (G (THEN (a, X b)), response a b)

(* precedence(A, B) = F(B) → ((¬B)UA) *)
let precedence a b = THEN (F b, U (NOT b, a))

(* alternatePrecedence(A, B) = precedence(A, B) ∧ G(B → X(precedence(A, B)) *)
let alternate_precedence a b =
  AND (precedence a b, G (THEN (b, X (precedence a b))))

(* chainPrecedence(A, B) = precedence(A, B) ∧ G(X(B) → A) *)
let chain_precedence a b = AND (precedence a b, G (THEN (X b, a)))

(* succession(A, B) = response(A, B) ∧ precedence(A, B) *)
let succession a b = AND (response a b, precedence a b)

(* chainSuccession(A, B) = G(A ↔ X(B)) *)
let chain_succession a b = G (IFF (a, X b))

(* 
 * alternateSuccession(A, B) = 
 * alternateResponse(A, B) ∧ alternatePrecedence(A, B) 
 *)
let alternate_succession a b =
  AND (alternate_response a b, alternate_precedence a b)

(* notRespondedExistence(A, B) =  F(A) → F(B) *)
let not_responded_existence a b = THEN (F a, F b)

(* notResponse(A, B) = G(A → ¬F(B)) *)
let not_response a b = G (THEN (a, NOT (F b)))

(* notPrecedence(A, B) = G(F (B) → ¬A) *)
let not_precedence a b = G (THEN (F b, NOT a))

(* notChainResponse(A,B) = G(A → ¬X(B)) *)
let not_chain_response a b = G (THEN (a, NOT (X b)))

(* notChainPrecedence(A, B) = G(X(B) → ¬A) *)
let not_chain_precedence a b = G (THEN (X b, NOT a))

(* notCoExistence(A, B) = ¬(F(A) ∧ F(B)) *)
let not_co_existence a b = NOT (AND (F a, F b))

(* choice(A, B) = F(A) ∨ F(B) *)
let choice a b = OR (F a, F b)

(* exclusiveChoice(A, B) = (F(A) ∨ F(B)) ∧ ¬(F(A) ∧ F(B)) *)
let exclusive_choice a b = AND (OR (F a, F b), NOT (AND (F a, F b)))

(* print and string utility functions *)
let rec string_of_ltl t =
  match t with
  | V s -> s
  | G t0 -> "G(" ^ string_of_ltl t0 ^ ")"
  | F t0 -> "F(" ^ string_of_ltl t0 ^ ")"
  | X t0 -> "X(" ^ string_of_ltl t0 ^ ")"
  | NOT t0 -> "NOT(" ^ string_of_ltl t0 ^ ")"
  | U (t0, t1) -> "U(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | THEN (t0, t1) -> "THEN(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | AND (t0, t1) -> "AND(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | OR (t0, t1) -> "OR(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | IFF (t0, t1) -> "IFF(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"

let rec compare t1 t2 =
  match (t1, t2) with
  | V s1, V s2 -> String.compare s1 s2
  | G t1, G t2 -> compare t1 t2
  | F t1, F t2 -> compare t1 t2
  | X t1, X t2 -> compare t1 t2
  | NOT t1, NOT t2 -> compare t1 t2
  | U (a1, b1), U (a2, b2) ->
      let c = compare a1 a2 in
      if c = 0 then compare b1 b2 else c
  | THEN (a1, b1), THEN (a2, b2) ->
      let c = compare a1 a2 in
      if c = 0 then compare b1 b2 else c
  | IFF (a1, b1), IFF (a2, b2) ->
      let c = compare a1 a2 in
      if c = 0 then compare b1 b2 else c
  | AND (a1, b1), AND (a2, b2) ->
      let c = compare a1 a2 in
      if c = 0 then compare b1 b2 else c
  | OR (a1, b1), OR (a2, b2) ->
      let c = compare a1 a2 in
      if c = 0 then compare b1 b2 else c
  | _ -> String.compare (string_of_ltl t1) (string_of_ltl t2)

let string_of_ltl_list (ltls : term list) : string =
  let rec get_ltl_string_aux ltls acc =
    match ltls with
    | [] -> acc
    | h :: t -> get_ltl_string_aux t (acc ^ ", " ^ string_of_ltl h)
  in
  get_ltl_string_aux ltls ""

let print_ltls =
  let v0 = V "t0" in
  let v1 = V "t1" in
  let v = string_of_ltl v0 in
  let g = string_of_ltl (G v0) in
  let f = string_of_ltl (F v0) in
  let x = string_of_ltl (X v0) in
  let n = string_of_ltl (NOT v0) in
  let u = string_of_ltl (U (v0, v1)) in
  let t = string_of_ltl (THEN (v0, v1)) in
  let a = string_of_ltl (AND (v0, v1)) in
  let o = string_of_ltl (OR (v0, v1)) in
  let i = string_of_ltl (IFF (v0, v1)) in
  Printf.printf
    "{|\n\
    \   All possible ltl terms:\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   %s\n\
    \   PROB_DECLARE -> Linear Temporal:\n\
    \   existence(a) = %s\n\
    \   absence(a) = %s\n\
    \   atLeast(a, n) = %s\n\
    \   atMost(a, n) = %s\n\
    \   init(a) = %s\n\
    \   last(a) = %s\n\
    \   respondedExistence(a, b) = %s\n\
    \   response(a, b) = %s\n\
    \   alternateResponse(a, b) = %s\n\
    \   chainResponse(a, b) = %s\n\
    \   precedence(a, b) = %s\n\
    \   alternatePrecedence(a, b) = %s\n\
    \   chainPrecedence(a, b) = %s\n\
    \   succession(a, b) = %s\n\
    \   chainSuccession(a, b) = %s\n\
    \   alternateSuccession(a, b) = %s\n\
    \   notRespondedExistence(a, b) = %s\n\
    \   notResponse(a, b) = %s\n\
    \   notPrecedence(a, b) = %s\n\
    \   notChainResponse(a, b) = %s\n\
    \   notChainPrecedence(a, b) = %s\n\
    \   coExistence(a, b) = %s\n\
    \   notCoExistence(a, b) = %s\n\
    \   choice(a, b) = %s\n\
    \   exclusiveChoice(a, b) = %s\n\
    \     |}" v g f x n u t a o i
    (string_of_ltl (existence v0))
    (string_of_ltl (absence v0))
    (string_of_ltl (at_least v0 5))
    (string_of_ltl (at_most v0 5))
    (string_of_ltl (init v0))
    (string_of_ltl (last v0))
    (string_of_ltl (responded_existence v0 v1))
    (string_of_ltl (response v0 v1))
    (string_of_ltl (alternate_response v0 v1))
    (string_of_ltl (chain_response v0 v1))
    (string_of_ltl (precedence v0 v1))
    (string_of_ltl (alternate_precedence v0 v1))
    (string_of_ltl (chain_precedence v0 v1))
    (string_of_ltl (succession v0 v1))
    (string_of_ltl (chain_succession v0 v1))
    (string_of_ltl (alternate_succession v0 v1))
    (string_of_ltl (not_responded_existence v0 v1))
    (string_of_ltl (not_response v0 v1))
    (string_of_ltl (not_precedence v0 v1))
    (string_of_ltl (not_chain_response v0 v1))
    (string_of_ltl (not_chain_precedence v0 v1))
    (string_of_ltl (co_existence v0 v1))
    (string_of_ltl (not_co_existence v0 v1))
    (string_of_ltl (choice v0 v1))
    (string_of_ltl (exclusive_choice v0 v1))
