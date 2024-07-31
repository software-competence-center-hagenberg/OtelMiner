open Opentelemetry_proto

module type Otel_to_prob_declare_converter = sig
  type ltl

  val convert : Trace.resource_spans list -> ltl list
  val get_ltl_string : ltl list -> string
end

module Converter: Otel_to_prob_declare_converter = struct
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
   * AND
   * OR
   * Note that there is no weak until -> instead use OR (U(a,b), G(V (a)))
   *)
  type ltl =
    V of string |
    G of ltl |
    F of ltl |
    N of ltl |
    NOT of ltl |
    U of ltl * ltl |
    THEN of ltl * ltl |
    AND of ltl * ltl |
    OR of ltl * ltl

  type p_ltl = (float * ltl)

  type prob_declare = (p_ltl list * p_ltl list)

  (*type span_tree =  Lf | N of span_tree_node
  and span_tree_node = {
      span: Trace.span;
      children: span_tree list
  }*)
  type span_tree = {
      span: Trace.span;
      children: span_tree list
  }

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
  
  let create_span_tree span= {span=span;children=[]}

  (*let rec insert_into_tree (rs: Trace.span) t =
    match t with
    | Lf -> t
    | N ({span; _} as node) -> 
      if span.span_id = rs.parent_span_id then N({node with children=(create_span_tree span)::node.children })
      (*else if children != [] then insert rs children*)
      else failwith ("insert_into_tree: orphan detected! (" ^ (String.of_bytes rs.span_id) ^ ")")*)

  (*let create_hash_table tree_nodes =
    let htbl = Hashtbl.create (List.length tree_nodes) in
    List.iter (fun node -> Hashtbl.add htbl node.span.span_id node) tree_nodes*)
  
  let split_roots_from_leafs nodes =
    let rec split_aux n r l =
      match n with
      | [] -> (r, l)
      | h :: t -> 
        if h.span.parent_span_id = Bytes.empty 
          then split_aux t (h::r) l 
      else split_aux t r (h::l)
    in split_aux nodes [] []

  let is_member parent_span_id tree = true

  let build_span_trees (resource_spans: Trace.resource_spans) =
    let spans = List.flatten (List.map (fun (s:Trace.scope_spans) -> s.spans) resource_spans.scope_spans) in
    let tree_nodes = List.map create_span_tree spans in
    let (roots, leaves) = split_roots_from_leafs tree_nodes in
    (*let htbl = create_hash_table tree_nodes in*)
    let rec build_span_trees_aux leaves l_tmp roots r_tmp =
      match leaves with
      | [] -> 
        if l_tmp = [] then roots 
        else build_span_trees_aux l_tmp [] roots r_tmp
      | leaf :: l_tl ->
        match roots with
        | [] -> 
          if leaves = [] 
            then if l_tmp = [] 
              then roots @ r_tmp
            else build_span_trees_aux l_tmp [] r_tmp []
          else build_span_trees_aux l_tl l_tmp r_tmp []
        | root :: a -> 
          if leaf.span.parent_span_id = root.span.parent_span_id
            then build_span_trees_aux l_tl l_tmp ({root with children=leaf::root.children}::a) r_tmp
          else if is_member leaf.span.parent_span_id root then insert_into_tree leaf root
          else build_span_trees_aux l_tl (leaf::l_tmp) r_tmp [] (* FIXME divide and conquer*)
        
          

    in build_span_trees_aux leaves [] roots []

  let map_to_ltl (rs : Trace.resource_spans): ltl =
    (*let span_trees = build_span_trees rs*)
    let l = List.length rs.scope_spans in
      if l > 1 then V "a" 
      else if l > 2 then G (V "b")
      else if l > 3 then F (V "a")
      else if l > 4 then N (V "a")
      else if l > 5 then NOT (V "b")
      else if l > 6 then U (V "a", V "b")
      else if l > 7 then AND (V "a", V "b")
      else OR (V "a", V "b")

 (*let parse (resource_spans_string : string) : Trace.resource_spans =
  Trace.make_resource_spans ~attrs: resource_spans_string
  let resource_spans = (Opentelemetry_proto.Trace.resource_spans) [@@deriving yojson] in
  resource_spans*)
  (*let lex_buf = BatLexing.from_string resource_spans_string in*)

(*    let rec parse_aux rss acc = *)
(*      match rss with *)
(*      | "resource" -> acc *)
(*      | _ -> failwith "parse: wrong format!" *)
(*  in parse_aux resource_spans_string Trace.make_resource_spans. *)
(*let resource_spans_decoder rss: Trace.resource_spans =
  Json_decoder.decode_resource_spans rss *)
    
  let convert (resource_spans : Trace.resource_spans list) : ltl list = 
    (*map_to_ltl resource_spans*)
    let rec convert_aux l k =
      match l with
      | [] -> k []
      | h :: t -> convert_aux t (fun a -> k ((map_to_ltl h):: a))
    in convert_aux resource_spans (fun x -> x)


  let get_ltl_string (ltls : ltl list) : string =
    (*let resource_spans = Trace.make_resource_spans resource_spans_string in*)
    (*string_of_ltl ltl*)
    (*let ltls = convert resource_spans in*)
    let rec get_ltl_string_aux ltls acc=
      match ltls with
      | [] -> acc
      | h :: t -> get_ltl_string_aux t (acc ^ ", " ^ string_of_ltl h)
    in get_ltl_string_aux ltls ""


end