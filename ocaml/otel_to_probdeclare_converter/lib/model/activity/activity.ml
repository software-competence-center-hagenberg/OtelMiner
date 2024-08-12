open Util

let rec find_next a = function
  | [] -> []
  | h :: t -> if h = a then h :: t else find_next a t

let rec find_next_a_or_b a b = function
  | [] -> []
  | h :: t -> if h = a || h = b then h :: t else find_next_a_or_b a b t

let is_relation (a : string) (b : string) (activities : string list)
    abortion_predicate find_next_function continuation_predicate : bool =
  let rec is_relation_aux (a : string) (b : string) (activities : string list)
      (cur : string) (found : bool) : bool =
    match activities with
    | [] -> found
    | h :: t ->
        if h = cur then
          if cur = a then is_relation_aux a b t b found
          else is_relation_aux a b t a true
        else if abortion_predicate cur h t then false
        else
          let next = find_next_function t in
          if next = [] then found
          else
            continuation_predicate next (fun () ->
                is_relation_aux a b t cur found)
  in
  is_relation_aux a b activities a false

let is_chain_succession (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur _h _t -> cur = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next f -> if List.hd next = a then f () else false)

let is_chain_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b || (cur = a && h = b))
    (fun t -> find_next a t)
    (fun _next f -> f ())

let is_chain_precedence (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities (* FIXME detects wrong a,c in unit test*)
    (fun cur _h t -> cur = a && (not (t = [])) && not (List.hd t = b))
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
    (fun cur h t -> cur = b && h != a && (not (t = [])) && List.hd t = b)
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

let rec get_next_to_check (activities : string list) (checked : StringSet.t) :
    (string * string list) option =
  match activities with
  | [] -> None
  | h :: t ->
      if StringSet.(checked |> mem h) then get_next_to_check t checked
      else Some (h, t)
