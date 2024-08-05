open Opentelemetry_proto

type span_tree_node = { span : Trace.span; children : span_tree_node list }

let string_of_span_id node = String.of_bytes node.span.span_id

(* Helper function to repeat a string n times *)
(*let rec repeat_string s n =
    if n <= 0 then "" else s ^ repeat_string s (n - 1)

  (* Function to construct the tree representation *)
  let rec string_of_span_tree ?(indent_level=0) (node : span_tree_node) : string =
    let span_id = string_of_span_id node in
    let indent = repeat_string " " (2 * indent_level) in
    match node.children with
    | [] -> indent ^ span_id ^ "\n"  (* Node with no children *)
    | [child] ->  (* Node with one child *)
        indent ^ span_id ^ "\n" ^
        indent ^ "|\n" ^
        indent ^ "|\n" ^
        indent ^ "|\n" ^
        string_of_span_tree ~indent_level:(indent_level + 1) child
    | children ->  (* Node with two or more children *)
        let children_strings = List.map (string_of_span_tree ~indent_level:(indent_level + 1)) children in
        let connector =
          if List.length children = 2 then
            indent ^ "+----+\n" ^
            indent ^ "|    |\n"
          else
            indent ^ "+----+" ^ repeat_string "---" (List.length children - 2) ^ "+\n" ^
            indent ^ "|    " ^ repeat_string "    " (List.length children - 2) ^ "|\n"
        in
        indent ^ span_id ^ "\n" ^
        indent ^ "|\n" ^
        connector ^
        String.concat "" children_strings*)

let create_span_tree_node span = { span; children = [] }

(*
 * Extracts all spans contained in the scope spans of a single resource_spans 
 * object
 *)
let extract_spans (resource_spans : Trace.resource_spans) =
  let scs = resource_spans.scope_spans in
  List.flatten (List.map (fun (s : Trace.scope_spans) -> s.spans) scs)

(*
 * Creates a span_tree_node for every span and builds a list of roots and nodes.
 * returns (r, n) where r is a list of roots and n is a list of non-root nodes. 
 * Note: The original order of spans is kept.
 *)
let generate_nodes (spans : Trace.span list) =
  let rec gen_aux (spans : Trace.span list) fr fn =
    match spans with
    | [] -> (fr [], fn [])
    | s :: t ->
        if s.parent_span_id = Bytes.empty then
          gen_aux t (fun a -> fr (create_span_tree_node s :: a)) fn
        else gen_aux t fr (fun a -> fn (create_span_tree_node s :: a))
  in
  gen_aux spans (fun x -> x) (fun x -> x)

(*
 * Finds and returns span given span_id in given tree. Returns None if not
 * found.
 *)
let rec find_span span_id tree =
  if tree.span.span_id = span_id then Some tree
  else
    let rec find_in_children = function
      | [] -> None
      | child :: rest ->
          let span = find_span span_id child in
          if span = None then find_in_children rest else span
    in
    find_in_children tree.children

(*
 * Inserts given subtree at node with span_id = ps_id of given tree.
 * ps_id ... parent_span_id
 *)
let rec insert_at ps_id subtree tree =
  if tree.span.span_id = ps_id then
    { tree with children = subtree :: tree.children }
  else
    let new_children = List.map (insert_at ps_id subtree) tree.children in
    { tree with children = new_children }

(* Builds all span trees with given roots and nodes. *)
let rec build_span_trees nodes n_tmp roots r_tmp =
  match nodes with
  | [] ->
      if n_tmp = [] then List.rev_append r_tmp roots
      else build_span_trees n_tmp [] (List.rev_append r_tmp roots) []
  | node :: n -> (
      match roots with
      | [] ->
          failwith
            ("build_span_trees: could not build tree! orphan: "
           ^ string_of_span_id node)
      | root :: r -> (
          match find_span node.span.parent_span_id root with
          | None -> build_span_trees nodes (node :: n_tmp) r (root :: r_tmp)
          | Some target_parent ->
              let target_id = target_parent.span.span_id in
              let new_root = insert_at target_id node root in
              build_span_trees n n_tmp (new_root :: r) r_tmp))

(* 
 * Takes resources_spans object. 
 * Extracts all spans contained in its scope_spans.
 * Generates (root) nodes out of them and
 * builds all possible trees.
 * Fails if an orphan is included!
 * TODO: check if needed for list of span trees!
 *)
let create_span_trees (resource_spans : Trace.resource_spans) :
    span_tree_node list =
  let spans = extract_spans resource_spans in
  let roots, nodes = generate_nodes spans in
  build_span_trees nodes [] roots []
