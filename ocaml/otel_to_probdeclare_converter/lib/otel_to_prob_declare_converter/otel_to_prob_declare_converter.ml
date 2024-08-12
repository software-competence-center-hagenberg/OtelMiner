open Opentelemetry_proto
open Util
open Ltl

let create_val (node : Span_tree.span_tree_node) = V node.span.name

let _map_to_ltl (root : Span_tree.span_tree_node) : term =
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
(*
let create_ltls (resource_spans : Trace.resource_spans) : term list =
  let span_trees = Span_tree.create_span_trees resource_spans in
  let rec create_ltls_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t -> create_ltls_aux t (fun a -> f (map_to_ltl h :: a))
  in
  create_ltls_aux span_trees (fun x -> x)
*)

let extract_activity (node : Span_tree.span_tree_node) = node.span.name

let extract_activites (nodes : Span_tree.span_tree_node list) =
  List.map extract_activity nodes

let map_existence (activities : StringSet.t) =
  DeclareSet.(
    of_list
      (List.map (fun a -> Declare.EXISTENCE a) (StringSet.elements activities)))

let map_choices (parallel : string list) : DeclareSet.t =
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
 *)

let rec find_next a = function
  | [] -> []
  | h :: t -> if h = a then h :: t else find_next a t

let rec find_next_a_or_b a b = function
  | [] -> []
  | h :: t -> if h = a || h = b then h :: t else find_next_a_or_b a b t

(*
let rec is_chain_succession a b future cur =
  match future with
  | [] -> true
  | h :: t ->
      if h = cur then
        if a = cur then is_chain_succession a b t b
        else is_chain_succession a b t a
      else if cur = a then false
      else
        let next = find_next_a_or_b a b t in
        if next = [] then true
        else if List.hd next = a then is_chain_succession a b t b
        else false
*)
(*
let rec is_chain_response a b future cur =
  match future with
  | [] -> true
  | h :: t ->
      if h = cur then
        if a = cur then is_chain_response a b t b else is_chain_response a b t a
      else if cur = b then false
      else
        let next = find_next a t in
        if next = [] then true else is_chain_response a b t b
*)

(*
let rec is_chain_precedence a b future cur =
  match future with
  | [] -> true
  | h :: t ->
      if h = cur then
        if a = cur then is_chain_precedence a b t b
        else is_chain_precedence a b t a
      else if cur = a then false
      else
        let next = find_next a t in
        if next = [] then true else is_chain_precedence a b t b
*)

let is_relation (a : string) (b : string) (activities : string list)
    abortion_predicate find_next_function continuation_function : bool =
  let rec is_relation_aux (a : string) (b : string) (future : string list)
      (cur : string) =
    match future with
    | [] -> true
    | h :: t ->
        if h = cur then
          if cur = a then is_relation_aux a b t b else is_relation_aux a b t a
        else if abortion_predicate cur h t then false
        else
          let next = find_next_function t in
          if next = [] then true
          else continuation_function next (fun () -> is_relation_aux a b t cur)
  in
  is_relation_aux a b activities a

let is_chain_succession (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur _h _t -> cur = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next f -> if List.hd next = a then f () else false)

let is_chain_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b && h != a)
    (fun t -> find_next a t)
    (fun _next f -> f ())

let is_chain_precedence (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h != b)
    (fun t -> find_next a t)
    (fun _next f -> f ())

let is_alternate_succession (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun _cur h _t -> h = a || h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next f -> if List.hd next = a then f () else false)

let is_alternate_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h t -> cur = b && h = a && List.hd t != b)
    (fun t -> find_next a t)
    (fun _next f -> f ())

let is_alternate_precedence (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun cur h t -> cur = b && h != a && List.hd t = b)
    (fun t -> find_next a t)
    (fun _next f -> f ())

(*
 * FIXME: check if succession is true per default for all a b in a trace 
 *        where a < b
 *)
let is_succession (a : string) (b : string) (activities : string list) =
  is_relation a b activities
    (fun cur h _t -> (cur = b && h = a) || (cur = a && h = b))
    (fun t -> t)
    (fun _next f -> f ())

let is_response (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur _h t -> cur = a && t = [])
    (fun t -> t)
    (fun _next f -> f ())

let is_precedence (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h = b)
    (fun t -> t)
    (fun _next f -> f ())

let determine_relation (a : string) (b : string) (activities : string list) :
    Declare.t option =
  if is_chain_succession a b activities then
    Some (Declare.CHAIN_SUCCESSION (a, b))
  else if is_chain_response a b activities then
    Some (Declare.CHAIN_RESPONSE (a, b))
  else if is_chain_precedence a b activities then
    Some (Declare.CHAIN_PRECEDENCE (a, b))
  else if is_alternate_succession a b activities then
    Some (Declare.ALTERNATE_SUCCESSION (a, b))
  else if is_alternate_response a b activities then
    Some (Declare.ALTERNATE_RESPONSE (a, b))
  else if is_alternate_precedence a b activities then
    Some (Declare.ALTERNATE_PRECEDENCE (a, b))
  else if is_succession a b activities then Some (Declare.SUCCESSION (a, b))
  else if is_response a b activities then Some (Declare.RESPONSE (a, b))
  else if is_precedence a b activities then Some (Declare.PRECEDENCE (a, b))
  else None

let rec get_next_to_check (activities : string list) (checked : StringSet.t) :
    (string * string list) option =
  match activities with
  | [] -> None
  | h :: t ->
      if StringSet.(checked |> mem h) then get_next_to_check t checked
      else Some (h, t)

let map_relations (activities : string list) : DeclareSet.t =
  let rec map_relations_aux a activities tmp checked acc =
    match activities with
    | [] -> (
        if tmp = [] then acc
        else
          let new_to_check = List.rev tmp in
          match get_next_to_check new_to_check checked with
          | None -> acc
          | Some (next_a, next_to_check) ->
              map_relations_aux next_a next_to_check []
                StringSet.(checked |> add a)
                acc)
    | b :: t -> (
        if b = a then map_relations_aux a t tmp checked acc
        else
          match determine_relation a b activities with
          | None -> map_relations_aux a t (b :: tmp) checked acc
          | Some relation ->
              map_relations_aux a t (b :: tmp) checked
                DeclareSet.(acc |> add relation))
  in
  map_relations_aux (List.hd activities) activities [] StringSet.empty
    DeclareSet.empty

type mapping_conf = {
  p : string list; (* previous activities [a(n-1), a(n-2), a(n-3), ..., a0] *)
  a : StringSet.t; (* activities FIXME change to Hashtbl with int counter! *)
  c : DeclareSet.t; (* Set of all constraints *)
}

let initialize_conf (root : Span_tree.span_tree_node) : mapping_conf =
  let choices = map_choices (extract_activites root.children) in
  {
    p = [ root.span.name ];
    a = StringSet.(empty |> add root.span.name);
    c = DeclareSet.(choices |> add (Declare.INIT root.span.name));
  }

let add_relations (conf : mapping_conf) : mapping_conf =
  let activities = List.rev conf.p in
  let relations = map_relations activities in
  { conf with c = DeclareSet.(union conf.c relations) }

let add_last (children : Span_tree.span_tree_node list) (activity : string)
    (constraints : DeclareSet.t) : DeclareSet.t =
  if children = [] then DeclareSet.(constraints |> add (Declare.LAST activity))
  else constraints

let configure_child (node : Span_tree.span_tree_node) (conf : mapping_conf) :
    mapping_conf =
  let child_activities = extract_activites node.children in
  let choices = map_choices child_activities in
  let activity = extract_activity node in
  let constraints = add_last node.children activity choices in
  {
    p = activity :: conf.p;
    a = StringSet.(conf.a |> add activity);
    c = DeclareSet.(union conf.c constraints);
  }

let map_to_declare (root : Span_tree.span_tree_node) : Declare.t list =
  let rec map_children (conf : mapping_conf)
      (children : Span_tree.span_tree_node list) =
    match children with
    | [] -> add_relations conf
    | child :: rest ->
        let child_conf = configure_child child conf in
        let child_conf = map_children child_conf child.children in
        if rest = [] then child_conf
        else map_children { conf with a = child_conf.a; c = child_conf.c } rest
  in
  let init_conf = initialize_conf root in
  let final_conf = map_children init_conf root.children in
  DeclareSet.elements
    DeclareSet.(union final_conf.c (map_existence final_conf.a)) (* FIXME improve map_existence and union*)

(*let convert (resource_spans : Trace.resource_spans list) : term list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_ltls h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))*)
let create_declare_constraints (resource_spans : Trace.resource_spans) :
    Declare.t list list =
  let span_trees = Span_tree.create_span_trees resource_spans in
  let rec create_ltls_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t -> create_ltls_aux t (fun a -> f (map_to_declare h :: a))
  in
  create_ltls_aux span_trees (fun x -> x)

let convert (resource_spans : Trace.resource_spans list) : Declare.t list list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_declare_constraints h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))
