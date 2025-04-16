(*
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
 * coExistence(A, B) = F(A) ↔ F(B)
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
 * notRespondedExistence(A, B) =  ¬(F(A) → F(B))
 * notResponse(A, B) = G(A → ¬F(B))
 * notPrecedence(A, B) = G(F (B) → ¬A)
 * notChainResponse(A, B) = G(A → ¬X(B))
 * notChainPrecedence(A, B) = G(X(B) → ¬A)
 * notCoExistence(A, B) = ¬(F(A) ∧ F(B))
 * choice(A, B) = F(A) ∨ F(B)
 * exclusiveChoice(A, B) = (F(A) ∨ F(B)) ∧ ¬(F(A) ∧ F(B))
 * -----------------------------------------------------------------------------
 * addition: exactly(A, n) = atLeast(A,n) ∧ atMost(A,n)
 * -----------------------------------------------------------------------------
 *)
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

let map_declare_to_ltl d =
  match d with
  | EXISTENCE a -> Ltl.existence (V a)
  | ABSENCE a -> Ltl.absence (V a)
  | AT_LEAST (a, n) when n >= 0 -> Ltl.at_least (V a) n
  | AT_MOST (a, n) when n >= 0 -> Ltl.at_most (V a) n
  | EXACTLY (a, n) when n >= 0 -> Ltl.exactly (V a) n
  | INIT a -> Ltl.init (V a)
  | LAST a -> Ltl.last (V a)
  | RESPONDED_EXISTENCE (a, b) -> Ltl.responded_existence (V a) (V b)
  | CO_EXISTENCE (a, b) -> Ltl.co_existence (V a) (V b)
  | RESPONSE (a, b) -> Ltl.response (V a) (V b)
  | ALTERNATE_RESPONSE (a, b) -> Ltl.alternate_response (V a) (V b)
  | CHAIN_RESPONSE (a, b) -> Ltl.chain_response (V a) (V b)
  | PRECEDENCE (a, b) -> Ltl.precedence (V a) (V b)
  | ALTERNATE_PRECEDENCE (a, b) -> Ltl.alternate_precedence (V a) (V b)
  | CHAIN_PRECEDENCE (a, b) -> Ltl.chain_precedence (V a) (V b)
  | SUCCESSION (a, b) -> Ltl.succession (V a) (V b)
  | ALTERNATE_SUCCESSION (a, b) -> Ltl.alternate_succession (V a) (V b)
  | CHAIN_SUCCESSION (a, b) -> Ltl.chain_succession (V a) (V b)
  | NOT_RESPONDED_EXISTENCE (a, b) -> Ltl.not_responded_existence (V a) (V b)
  | NOT_RESPONSE (a, b) -> Ltl.not_response (V a) (V b)
  | NOT_CHAIN_RESPONSE (a, b) -> Ltl.not_chain_response (V a) (V b)
  | NOT_PRECEDENCE (a, b) -> Ltl.not_precedence (V a) (V b)
  | NOT_CHAIN_PRECEDENCE (a, b) -> Ltl.not_chain_precedence (V a) (V b)
  | NOT_COEXISTENCE (a, b) -> Ltl.not_co_existence (V a) (V b)
  | CHOICE (a, b) -> Ltl.choice (V a) (V b)
  | EXCLUSIVE_CHOICE (a, b) -> Ltl.exclusive_choice (V a) (V b)
  | _ -> failwith "invalid constraint -> negative n!"

let to_string d =
  let pf = Printf.sprintf in
  match d with
  | EXISTENCE a -> pf "EXISTENCE(%s)" a
  | ABSENCE a -> pf "ABSENCE(%s)" a
  | AT_LEAST (a, n) -> pf "AT_LEAST(%s, %d)" a n
  | AT_MOST (a, n) -> pf "AT_MOST(%s, %d)" a n
  | EXACTLY (a, n) -> pf "EXACTLY(%s, %d)" a n
  | INIT a -> pf "INIT(%s)" a
  | LAST a -> pf "LAST(%s)" a
  | RESPONDED_EXISTENCE (a, b) -> pf "RESPONDED_EXISTENCE(%s, %s)" a b
  | CO_EXISTENCE (a, b) -> pf "CO_EXISTENCE(%s, %s)" a b
  | RESPONSE (a, b) -> pf "RESPONSE(%s, %s)" a b
  | ALTERNATE_RESPONSE (a, b) -> pf "ALTERNATE_RESPONSE(%s, %s)" a b
  | CHAIN_RESPONSE (a, b) -> pf "CHAIN_RESPONSE(%s, %s)" a b
  | PRECEDENCE (a, b) -> pf "PRECEDENCE(%s, %s)" a b
  | ALTERNATE_PRECEDENCE (a, b) -> pf "ALTERNATE_PRECEDENCE(%s, %s)" a b
  | CHAIN_PRECEDENCE (a, b) -> pf "CHAIN_PRECEDENCE(%s, %s)" a b
  | SUCCESSION (a, b) -> pf "SUCCESSION(%s, %s)" a b
  | ALTERNATE_SUCCESSION (a, b) -> pf "ALTERNATE_SUCCESSION(%s, %s)" a b
  | CHAIN_SUCCESSION (a, b) -> pf "CHAIN_SUCCESSION(%s, %s)" a b
  | NOT_RESPONDED_EXISTENCE (a, b) -> pf "NOT_RESPONDED_EXISTENCE(%s, %s)" a b
  | NOT_RESPONSE (a, b) -> pf "NOT_RESPONSE(%s, %s)" a b
  | NOT_CHAIN_RESPONSE (a, b) -> pf "NOT_CHAIN_RESPONSE(%s, %s)" a b
  | NOT_PRECEDENCE (a, b) -> pf "NOT_PRECEDENCE(%s, %s)" a b
  | NOT_CHAIN_PRECEDENCE (a, b) -> pf "NOT_CHAIN_PRECEDENCE(%s, %s)" a b
  | NOT_COEXISTENCE (a, b) -> pf "NOT_COEXISTENCE(%s, %s)" a b
  | CHOICE (a, b) -> pf "CHOICE(%s, %s)" a b
  | EXCLUSIVE_CHOICE (a, b) -> pf "EXCLUSIVE_CHOICE(%s, %s)" a b

let list_to_string (l : t list) : string =
  let elements = List.map to_string l in
  "[" ^ (String.concat "; " elements) ^ "]"

let list_list_to_string (ll : t list list) : string =
  let lists = List.map list_to_string ll in
  "[" ^ (String.concat "; " lists) ^ "]"

let list_to_json_string_list (dl : t list) : Yojson.Basic.t =
  `List (List.map (fun d -> `String (to_string d)) dl)

let list_list_to_json_string_list_list (dll : t list list) : Yojson.Basic.t =
  `List (List.map list_to_json_string_list dll)

let compare_pairs (a1, b1) (a2, b2) =
  let cmp1 = String.compare a1 a2 in
  if cmp1 <> 0 then cmp1 else String.compare b1 b2

let compare d1 d2 =
  let cp = compare_pairs in
  let si = string_of_int in
  match (d1, d2) with
  | EXISTENCE a, EXISTENCE b -> String.compare a b
  | ABSENCE a, ABSENCE b -> String.compare a b
  | AT_LEAST (a, n), AT_LEAST (b, m) -> cp (a, si n) (b, si m)
  | AT_MOST (a, n), AT_MOST (b, m) -> cp (a, si n) (b, si m)
  | EXACTLY (a, n), EXACTLY (b, m) -> cp (a, si n) (b, si m)
  | INIT a, INIT b -> String.compare a b
  | LAST a, LAST b -> String.compare a b
  | RESPONDED_EXISTENCE (a, b), RESPONDED_EXISTENCE (c, d) -> cp (a, b) (c, d)
  | CO_EXISTENCE (a, b), CO_EXISTENCE (c, d) -> cp (a, b) (c, d)
  | RESPONSE (a, b), RESPONSE (c, d) -> cp (a, b) (c, d)
  | ALTERNATE_RESPONSE (a, b), ALTERNATE_RESPONSE (c, d) -> cp (a, b) (c, d)
  | CHAIN_RESPONSE (a, b), CHAIN_RESPONSE (c, d) -> cp (a, b) (c, d)
  | PRECEDENCE (a, b), PRECEDENCE (c, d) -> cp (a, b) (c, d)
  | ALTERNATE_PRECEDENCE (a, b), ALTERNATE_PRECEDENCE (c, d) -> cp (a, b) (c, d)
  | CHAIN_PRECEDENCE (a, b), CHAIN_PRECEDENCE (c, d) -> cp (a, b) (c, d)
  | SUCCESSION (a, b), SUCCESSION (c, d) -> cp (a, b) (c, d)
  | ALTERNATE_SUCCESSION (a, b), ALTERNATE_SUCCESSION (c, d) -> cp (a, b) (c, d)
  | CHAIN_SUCCESSION (a, b), CHAIN_SUCCESSION (c, d) -> cp (a, b) (c, d)
  | NOT_RESPONDED_EXISTENCE (a, b), NOT_RESPONDED_EXISTENCE (c, d) ->
      cp (a, b) (c, d)
  | NOT_RESPONSE (a, b), NOT_RESPONSE (c, d) -> cp (a, b) (c, d)
  | NOT_CHAIN_RESPONSE (a, b), NOT_CHAIN_RESPONSE (c, d) -> cp (a, b) (c, d)
  | NOT_PRECEDENCE (a, b), NOT_PRECEDENCE (c, d) -> cp (a, b) (c, d)
  | NOT_CHAIN_PRECEDENCE (a, b), NOT_CHAIN_PRECEDENCE (c, d) -> cp (a, b) (c, d)
  | NOT_COEXISTENCE (a, b), NOT_COEXISTENCE (c, d) -> cp (a, b) (c, d)
  | CHOICE (a, b), CHOICE (c, d) -> cp (a, b) (c, d)
  | EXCLUSIVE_CHOICE (a, b), EXCLUSIVE_CHOICE (c, d) -> cp (a, b) (c, d)
  | _ -> String.compare (to_string d1) (to_string d2)
