open Opentelemetry_proto

(*
 * unary operators:
 * V ... value 
 * G ... globally
 * F ... eventually
 * N ... next
 * NOT
 *
 * binary operators:
 * U ... until
 * THEN ... implication
 * IFF ... if and only if (<->)
 * AND
 * OR
 * Note that there is no weak until -> instead use OR (U(a,b), G(V (a)))
 *)
type ltl =
  | V of string
  | G of ltl
  | F of ltl
  | N of ltl
  | NOT of ltl
  | U of ltl * ltl
  | THEN of ltl * ltl
  | IFF of ltl * ltl
  | AND of ltl * ltl
  | OR of ltl * ltl

(*type p_ltl = float * ltl
  type prob_declare = p_ltl list * p_ltl list*)

let rec string_of_ltl t =
  match t with
  | V s -> s
  | G t0 -> "G" ^ string_of_ltl t0
  | F t0 -> "F" ^ string_of_ltl t0
  | N t0 -> "N" ^ string_of_ltl t0
  | NOT t0 -> "NOT" ^ string_of_ltl t0
  | U (t0, t1) -> "U(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | THEN (t0, t1) -> "THEN(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | AND (t0, t1) -> "AND(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | OR (t0, t1) -> "OR(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
  | IFF (t0, t1) -> "IFF(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"

let map_to_ltl (span_tree : Span_tree.span_tree_node) : ltl list =
  let ltl = F (V span_tree.span.name) in
  [ltl]

let create_ltls (resource_spans : Trace.resource_spans) : ltl list =
  let span_trees = Span_tree.create_span_trees resource_spans in
  let rec create_ltls_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t -> create_ltls_aux t (fun a -> f (map_to_ltl h) :: a)
  in
  create_ltls_aux span_trees (fun x -> x)

let convert (resource_spans : Trace.resource_spans list) : ltl list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_ltls h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))

let get_ltl_string (ltls : ltl list) : string =
  (*let resource_spans = Trace.make_resource_spans resource_spans_string in*)
  (*string_of_ltl ltl*)
  (*let ltls = convert resource_spans in*)
  let rec get_ltl_string_aux ltls acc =
    match ltls with
    | [] -> acc
    | h :: t -> get_ltl_string_aux t (acc ^ ", " ^ string_of_ltl h)
  in
  get_ltl_string_aux ltls ""
