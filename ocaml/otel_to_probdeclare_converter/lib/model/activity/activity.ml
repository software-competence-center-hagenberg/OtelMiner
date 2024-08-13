open Util

let rec find_next a = function
  | [] -> []
  | h :: t -> if h = a then h :: t else find_next a t

let rec find_next_a_or_b a b = function
  | [] -> []
  | h :: t -> if h = a || h = b then h :: t else find_next_a_or_b a b t

let is_relation (a : string) (b : string) (activities : string list)
    abortion_predicate find_next_function continuation_predicate found_predicate
    : bool =
  let rec is_relation_aux (a : string) (b : string) (activities : string list)
      (cur : string) (cnt : int) : bool =
    match activities with
    | [] -> found_predicate cnt
    | h :: t ->
        if h = cur then
          if cur = a then is_relation_aux a b t b (cnt + 1)
          else is_relation_aux a b t a (cnt + 1)
        else if abortion_predicate cur h t then false
        else
          let next = find_next_function t in
          if next = [] then found_predicate cnt
          else
            continuation_predicate next cur (fun () ->
                is_relation_aux a b t cur cnt)
  in
  is_relation_aux a b activities a 0

let is_chain_succession (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b || h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)

let is_chain_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur _h _t -> cur = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)

let is_chain_precedence (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> a != b && cur = a && h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)

let is_alternate_succession (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun _cur h _t -> h = a || h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next cur f -> if List.hd next = cur then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 3 = 0)

let is_alternate_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b && h = a)
    (fun t -> find_next_a_or_b a b t)
    (fun next cur f -> if List.hd next = cur then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 3 = 0)

let is_alternate_precedence (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next cur f -> if List.hd next = cur then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 3 = 0)

(*
 * FIXME: check if succession is true per default for all a b in a trace 
 *        where a < b
 *)
let is_succession (a : string) (b : string) (activities : string list) =
  is_relation a b activities
    (fun cur h _t -> (cur = b && h = a) || (cur = a && h = b))
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

let is_response (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur h _t -> cur = b && h = a)
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

let is_precedence (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h = b)
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

let rec get_next_to_check (activities : string list) (checked : StringSet.t) :
    (string * string list) option =
  match activities with
  | [] -> None
  | h :: t ->
      if StringSet.(checked |> mem h) then get_next_to_check t checked
      else Some (h, t)
