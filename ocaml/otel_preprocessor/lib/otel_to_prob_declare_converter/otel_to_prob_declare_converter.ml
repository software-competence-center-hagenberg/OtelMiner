open Opentelemetry_proto
open Util
open Activity

(* 
 * Checks which DECLARE constraint applies to a b in activities. Checks in
 * descending order what applies. If CHAIN_SUCCESSI0N applies, then all the
 * other constraints apply anyway. IF not, then CHAIN_RESPONSE or
 * CHAIN_PRECEDENCE can still apply. If no chain constraints apply, then the
 * alternate constraints can still apply. Again if ALTERNATE_SUCCESSION
 * applies, then all the beneath apply anyway. 
 *)
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

(* 
 * Helper function to get the next activity to be checked as well as the rest
 * of the list after that. An activity to be checked is found if it is not a
 * member of the set of already checked activities.
 *)
let rec get_next_to_check (activities : string list) (checked : StringSet.t) :
    (string * string list) option =
  match activities with
  | [] -> None
  | h :: t ->
      if StringSet.(checked |> mem h) then get_next_to_check t checked
      else Some (h, t)

(*
 * Function that maps a given list of activities to a set of DECLARE relation
 * constraints.
 * Maps each combination of activities 
 *)
let map_relations (activities : string list) : DeclareSet.t =
  let rec map_relations_aux a ax tmp checked acc =
    match ax with
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
        if
          (* FIXME: for now case a = b not handled -> explicit impl if necessary *)
          b = a
        then map_relations_aux a t tmp checked acc
        else
          match determine_relation a b activities with
          | None -> map_relations_aux a t (b :: tmp) checked acc
          | Some relation ->
              map_relations_aux a t (b :: tmp) checked
                DeclareSet.(acc |> add relation))
  in
  map_relations_aux (List.hd activities) activities [] StringSet.empty
    DeclareSet.empty

(* 
 * Function that maps a given list of activities which are expected to be
 * siblings to the DECLARE choice constraint 
 *)
let map_choices (activities : string list) : DeclareSet.t =
  if activities = [] then DeclareSet.empty
  else
    let rec map_choice_aux (a : string) (activities : string list)
        (tmp : string list) (acc : DeclareSet.t) =
      let add_choice a s acc =
        if a = s then acc
        else if compare a s < 0 then
          DeclareSet.(acc |> add (Declare.CHOICE (a, s)))
        else DeclareSet.(acc |> add (Declare.CHOICE (s, a)))
      in
      match activities with
      | [] -> acc
      | s :: rest ->
          if rest = [] then
            match tmp with
            | [] -> add_choice a s acc
            | h :: t -> map_choice_aux h (t @ [ s ]) [] (add_choice a s acc)
          else map_choice_aux a rest (tmp @ [ s ]) (add_choice a s acc)
    in
    map_choice_aux (List.hd activities) (List.tl activities) [] DeclareSet.empty

let extract_activity (node : Span_tree.span_tree_node) = node.span.name

let extract_activites (nodes : Span_tree.span_tree_node list) =
  List.map extract_activity nodes

(*
 * Configuration used for mapping the traversal of a span_tree to a set of
 * DECLARE constraints.
 *)
type mapping_conf = {
  p : string list; (* tree traversal path [a(n-1), a(n-2), a(n-3), ..., a0] *)
  a : StringSet.t; (* activities FIXME change to Hashtbl with int counter! *)
  c : DeclareSet.t; (* Set of all constraints *)
}

(*
 * Helper function to create an initial configuration for a given span_tree
 * root.
 *)
let initialize_conf (root : Span_tree.span_tree_node) : mapping_conf =
  let choices = map_choices (extract_activites root.children) in
  {
    p = [ root.span.name ];
    a = StringSet.(empty |> add root.span.name);
    c = DeclareSet.(choices |> add (Declare.INIT root.span.name));
  }

(*
 * Function that adds all detectable relation constraints to given
 * configuration.
 *)
let add_relations (conf : mapping_conf) : mapping_conf =
  let activities = List.rev conf.p in
  let relations = map_relations activities in
  { conf with c = DeclareSet.union conf.c relations }

(*
 * Function that adds the DECLARE last constraint to the given set of
 * constraints iff the given node is a leaf
 *)
let add_last (children : Span_tree.span_tree_node list) (activity : string)
    (constraints : DeclareSet.t) : DeclareSet.t =
  if children = [] then DeclareSet.(constraints |> add (Declare.LAST activity))
  else constraints

(* Function that updates the current configuration for the current child. *)
let configure_child (node : Span_tree.span_tree_node) (conf : mapping_conf) :
    mapping_conf =
  let child_activities = extract_activites node.children in
  let choices = map_choices child_activities in
  let activity = extract_activity node in
  let constraints = add_last node.children activity choices in
  {
    p = activity :: conf.p;
    a = StringSet.(conf.a |> add activity);
    c = DeclareSet.union conf.c constraints;
  }

(* 
 * Creates an DECLARE existence constraint for each discovered activity and
 * adds it to the given set of constraints. 
 *)
let add_existence (activities : StringSet.t) (constraints : DeclareSet.t) =
  let existence =
    List.map (fun a -> Declare.EXISTENCE a) (StringSet.elements activities)
  in
  DeclareSet.union constraints (DeclareSet.of_list existence)

(*
 * Main function for mapping a span_tree to a set of DECLARE constraints.
 *)
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
  let final_constraints = add_existence final_conf.a final_conf.c in
  DeclareSet.elements final_constraints

(* Takes a resource_spans object, creates a list of span_trees out of them and
 * maps each tree to list of DECLARE constraints which reperesent the DECLARE
 * model of that tree 
 *)
let create_declare_constraints (span_trees : Span_tree.span_tree_node list) :
    Declare.t list list =
  let rec create_declare_constraints_aux (l : Span_tree.span_tree_node list) f =
    match l with
    | [] -> f []
    | h :: t ->
        create_declare_constraints_aux t (fun a -> f (map_to_declare h :: a))
  in
  create_declare_constraints_aux span_trees (fun x -> x)

(* 
 * Main function for mapping a list of resource spans toi their DECLARE mdoel
 * representations
 *)
let convert_resource_spans (resource_spans : Trace.resource_spans list) :
    Declare.t list list =
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t ->
        convert_aux t (fun a ->
            k
              (create_declare_constraints
                 (Span_tree.generate_span_trees_from_resource_spans h)
              :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))

let convert_trace_spans (trace_spans : Trace.span list) : Declare.t list list =
  let span_trees = Span_tree.generate_span_trees_from_spans trace_spans in
  create_declare_constraints span_trees

(*
open Ltl

let create_val (node : Span_tree.span_tree_node) = V node.span.name

let map_to_ltl (root : Span_tree.span_tree_node) : term =
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
  
let convert (resource_spans : Trace.resource_spans list) : term list =
  (*map_to_ltl resource_spans*)
  let rec convert_aux l k =
    match l with
    | [] -> k []
    | h :: t -> convert_aux t (fun a -> k (create_ltls h :: a))
  in
  List.flatten (convert_aux resource_spans (fun x -> x))
*)
