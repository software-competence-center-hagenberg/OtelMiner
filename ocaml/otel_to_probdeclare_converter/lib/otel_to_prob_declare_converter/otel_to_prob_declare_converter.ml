open Opentelemetry_proto

(*open Thread*)
open Ltl

let create_val (node : Span_tree.span_tree_node) = V node.span.name

(*let log_mapping_info root =
  "processing span_tree: \n" ^Span_tree.string_of_span_tree ^ (list_ltls ())*)

let map_to_ltl (root : Span_tree.span_tree_node) : ltl =
  (*Log.info (log_mapping_info root);*)
  print_ltls;
  let eventually_parent = F (V root.span.name) in
  let rec map_children (t : ltl) (children : Span_tree.span_tree_node list) =
    match children with
    | [] -> t
    | c0 :: [] -> map_children (U (t, F (create_val c0))) c0.children
    | c0 :: c1 :: rest ->
        let uec0 = map_children (U (t, F (create_val c0))) c0.children in
        let uec1 = map_children (U (t, F (create_val c1))) c1.children in
        let ac0c1 = AND (uec0, uec1) in
        if rest = [] then ac0c1 else AND (ac0c1, map_children t rest)
  in
  map_children eventually_parent root.children

let create_ltls (resource_spans : Trace.resource_spans) : ltl list =
  let span_trees = Span_tree.create_span_trees resource_spans in
  let rec create_ltls_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t -> create_ltls_aux t (fun a -> f (map_to_ltl h :: a))
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
