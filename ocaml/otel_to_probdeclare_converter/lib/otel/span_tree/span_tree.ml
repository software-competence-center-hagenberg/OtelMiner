open Opentelemetry_proto
open Amqp_client_async
open Thread

type span_tree_node = { span : Trace.span; children : span_tree_node list }

let rec pp_span_tree (fmt : Format.formatter) (span_tree : span_tree_node) =
  let pp_children fmt children =
    Format.fprintf fmt "[%a]"
      (Format.pp_print_list
         ~pp_sep:(fun fmt () -> Format.fprintf fmt "; ")
         pp_span_tree)
      children
  in
  Format.fprintf fmt "{\n    span = %a; \n    children = %a \n}\n"
    Otel_encoder.pp_span_custom span_tree.span pp_children
    span_tree.children

let string_of_node node =
  Format.flush_str_formatter (pp_span_tree Format.str_formatter node)

let string_of_nodes nodes =
  if nodes = [] then "[]" 
  else String.concat "; " (List.map (fun n -> string_of_node n) nodes)

(*let string_of_span_id node = String.of_bytes node.span.span_id*)
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
(*
let rec find_span_in_tree span_id tree =
  if tree.span.span_id = span_id then Some tree
  else
    let rec find_in_children = function
      | [] -> None
      | child :: rest ->
          let span = find_span_in_tree span_id child in
          if span = None then find_in_children rest else span
    in
    find_in_children tree.children
*)
(*
 * Inserts given subtree at node with span_id = ps_id of given tree.
 * ps_id ... parent_span_id
 *)
let rec insert_at ps_id subtrees tree =
  if tree.span.span_id = ps_id then { tree with children = subtrees }
  else
    let new_children = List.map (insert_at ps_id subtrees) tree.children in
    { tree with children = new_children }

let find_children parent nodes =
  let rec find_children_aux nodes fc fn =
    match nodes with
    | [] -> (fc [], fn [])
    | n :: t ->
        if n.span.parent_span_id = parent.span.span_id then
          find_children_aux t (fun a -> fc (n :: a)) fn
        else find_children_aux t fc (fun a -> fn (n :: a))
  in
  find_children_aux nodes (fun x -> x) (fun x -> x)

let build_tree root nodes =
  let rec build_tree_aux root node nodes =
    let rec map_children root children nodes =
      match children with
      | [] -> (root, nodes)
      | c :: t ->
          let new_root, other = build_tree_aux root c nodes in
          map_children new_root t other
    in
    let children, other = find_children node nodes in
    let new_root = insert_at node.span.span_id children root in
    if children = [] then (new_root, other)
    else map_children new_root children other
  in
  build_tree_aux root root nodes

(* Builds all span trees with given roots and nodes. *)
let build_span_trees nodes roots =
  let rec build_span_trees_aux nodes roots acc =
    match roots with
    | [] -> 
      Log.info "orphans remaining:\n";
      Log.info "%s" (string_of_nodes nodes);
      acc
    | root :: r ->
        let tree, n = build_tree root nodes in
        build_span_trees_aux n r (tree :: acc)
  in
  build_span_trees_aux nodes roots []

let generate_span_trees_from_spans (spans : Trace.span list) :
    span_tree_node list =
  let roots, nodes = generate_nodes spans in
  build_span_trees nodes roots

(* 
 * Takes resources_spans object. 
 * Extracts all spans contained in its scope_spans.
 * Generates (root) nodes out of them and
 * builds all possible trees.
 * Fails if an orphan is included!
 * TODO: check if needed for list of span trees!
 *)
let generate_span_trees_from_resource_spans
    (resource_spans : Trace.resource_spans) : span_tree_node list =
  let spans = extract_spans resource_spans in
  generate_span_trees_from_spans spans
