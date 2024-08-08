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
  p : string list; (* previous activities [a(n-1), a(n-2), a(n-3), ..., a0] *)
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

let map_choice (parallel : string list) : DeclareSet.t =
  if parallel = [] then DeclareSet.empty
  else
    let rec map_choice_aux (a : string) (activities : string list)
        (tmp : string list) (acc : DeclareSet.t) =
      match activities with
      | [] -> acc
      | s :: rest ->
          if rest = [] then map_choice_aux (List.hd tmp) (List.tl tmp) [] acc
          else
            map_choice_aux (List.hd rest) (List.tl rest) (s :: tmp)
              DeclareSet.(acc |> add (Declare.CHOICE (a, s)))
    in
    map_choice_aux (List.hd parallel) (List.tl parallel) [] DeclareSet.empty

(*
 * 0 ... no
 * 1 ... yes
 * 2 ... alternate
 * 3 ... chain
 *)
type relation_type = {
  r : int; (* relation *)
  p : int; (* precedence *)
  s : int; (* succession *)
}

let determine_relation (rt : relation_type) (a : string) (prev_a : string) =
  match rt with
  | { s = 3; _ } -> Declare.CHAIN_SUCCESSION (a, prev_a)
  | { s = 2; _ } -> Declare.ALTERNATE_SUCCESSION (a, prev_a)
  | { s = 1; _ } -> Declare.SUCCESSION (a, prev_a)
  | { r = 3; _ } -> Declare.CHAIN_RESPONSE (a, prev_a)
  | { p = 3; _ } -> Declare.CHAIN_PRECEDENCE (a, prev_a)
  | { r = 2; _ } -> Declare.ALTERNATE_RESPONSE (a, prev_a)
  | { p = 2; _ } -> Declare.ALTERNATE_PRECEDENCE (a, prev_a)
  | { r = 1; _ } -> Declare.RESPONSE (a, prev_a)
  | { p = 1; _ } -> Declare.PRECEDENCE (a, prev_a)
  | _ -> failwith "!"

let rec is_chain_succession (a : string) (b : string) (past : string list) (cur : string) =
  match past with
  | [] -> true
  | h :: t ->
    if h = cur then
      if a = cur then is_chain_succession a b t b
      else is_chain_succession a b t a
    else
      false (* FIXME *)

let check_relation (a : string) (b : string) (past : string list) : Declare.t =
  let rt = { r = 3; p = 3; s = 3 } in
  let rec check_relation_aux a b p c rt =
    match p with
    | [] -> determine_relation rt a b
    | ppa :: rest -> check_relation_aux a b p c rt (* FIXME *)
    (*if ppa = c then determine_relation rt a b else Declare.ABSENCE a*)
  in
  check_relation_aux a b past b rt

let map_relation (past : string list) (present : string list) : DeclareSet.t =
  let rec map_response_aux (past : string list) (present : string list) acc =
    match present with
    | [] -> acc
    | a :: tl ->
        let relation = check_relation a (List.hd past) (List.tl past) in
        map_response_aux past tl DeclareSet.(acc |> add relation)
  in
  map_response_aux past present DeclareSet.empty

let map_constraints (a0 : string list) (a1 : string list) : DeclareSet.t =
  let choices = map_choice a1 in
  let response = map_relation a0 a1 in
  DeclareSet.(union choices response)

let _map_to_declare (root : Span_tree.span_tree_node) : Declare.t list =
  let rec map_children (conf : mapping_conf)
      (children : Span_tree.span_tree_node list) =
    let child_activities = extract_activites children in
    let constraints = map_constraints conf.p child_activities in
    match children with
    | [] -> { conf with c = DeclareSet.(union conf.c (map_existence conf.a)) }
    | c0 :: rest ->
        let a_c0 = extract_activity c0 in
        (* FIXME check if last *)
        let conf_new =
          map_children
            {
              p = a_c0 :: conf.p;
              a = StringSet.(conf.a |> add a_c0);
              c = DeclareSet.(union conf.c constraints);
            }
            c0.children
        in
        if rest = [] then conf_new
        else map_children { conf with a = conf_new.a; c = conf_new.c } rest
  in
  let final_conf =
    map_children
      {
        p = [ root.span.name ];
        a = StringSet.(empty |> add root.span.name);
        c = DeclareSet.(empty |> add (Declare.INIT root.span.name));
      }
      root.children
  in
  DeclareSet.to_list final_conf.c

let convert (resource_spans : Trace.resource_spans list) : term list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_ltls h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))
