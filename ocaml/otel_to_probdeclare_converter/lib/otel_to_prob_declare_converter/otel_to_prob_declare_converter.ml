open Opentelemetry_proto

(*open Thread*)
open Ltl
(*open Prob_declare*)

let create_val (node : Span_tree.span_tree_node) = V node.span.name

let map_to_ltl (root : Span_tree.span_tree_node) : term =
  (*Log.info (log_mapping_info root);*)
  print_ltls;
  let eventually_parent = F (V root.span.name) in
  let rec map_children (t : term) (children : Span_tree.span_tree_node list) =
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

(*let map_to_ltl (root : Span_tree.span_tree_node) : term =
  (*Log.info (log_mapping_info root);*)
  print_ltls;
  let activities = StringSet.(empty |> add root.span.name) in
  let root_val = create_val root in
  let existence_root = EXISTENCE root_val in
  let rec map_children (a_prev: String) (a: StringSet.t) (constraints : declare_constraint list) (children : Span_tree.span_tree_node list) =
    match children with
    | [] -> t
    | c0 :: [] ->
      let val_c0 = create_val c0 in
      let last_c0 = LAST val_c0 in
      let e_c0 = EXISTENCE  val_c0 in
      let a = StringSet.(a |> add c0.span.name)
      map_children
    | c0 :: c1 :: rest -> t (*
        let uec0 = map_children (U (t, F (create_val c0))) c0.children in
        let uec1 = map_children (U (t, F (create_val c1))) c1.children in
        let ac0c1 = AND (uec0, uec1) in
        if rest = [] then ac0c1 else AND (ac0c1, map_children t rest)*)
  in
  map_children activities [existence_root] root.children*)

let create_ltls (resource_spans : Trace.resource_spans) : term list =
  let span_trees = Span_tree.create_span_trees resource_spans in
  let rec create_ltls_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t -> create_ltls_aux t (fun a -> f (map_to_ltl h :: a))
  in
  create_ltls_aux span_trees (fun x -> x)

module DeclareSet = Set.Make (Declare)
module StringSet = Set.Make (String)

type mapping_conf = {
  p : string; (* previous activity *)
  a : StringSet.t; (* activities *)
  c : DeclareSet.t; (* list of all constraints *)
}

let extract_activity (node : Span_tree.span_tree_node) = node.span.name

let extract_activites (nodes : Span_tree.span_tree_node list) =
  List.map extract_activity nodes

let map_existence (activities : StringSet.t) =
  DeclareSet.(
    of_list
      (List.map (fun a -> Declare.EXISTENCE a) (StringSet.to_list activities)))

let map_constraints (_a0 : string) (_a1 : string list) : DeclareSet.t =
  DeclareSet.empty

let _map_to_declare (root : Span_tree.span_tree_node) : mapping_conf =
  let rec map_children (conf : mapping_conf)
      (children : Span_tree.span_tree_node list) =
    let child_activities = extract_activites children in
    let constraints = map_constraints conf.p child_activities in
    match children with
    | [] -> { conf with c = DeclareSet.(union conf.c (map_existence conf.a)) }
    | c0 :: rest ->
        let a_c0 = extract_activity c0 in
        let activities = StringSet.(conf.a |> add a_c0) in
        (* FIXME check if last *)
        let conf_new =
          map_children
            {
              p = a_c0;
              a = activities;
              c = DeclareSet.(union conf.c constraints);
            }
            c0.children
        in
        if rest = [] then conf_new
        else map_children { conf with a = conf_new.a; c = conf_new.c } rest
  in
  map_children
    {
      p = root.span.name;
      a = StringSet.(empty |> add root.span.name);
      c = DeclareSet.(empty |> add (Declare.INIT root.span.name));
    }
    root.children

let convert (resource_spans : Trace.resource_spans list) : term list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_ltls h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))
