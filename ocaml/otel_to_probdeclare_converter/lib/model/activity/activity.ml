(* returns the remaining list including a as head, if found, else empty list*)
let rec find_next a = function
  | [] -> []
  | h :: t -> if h = a then h :: t else find_next a t

(*
 * returns the remaining list including a or b as head, if found, 
 * else empty list
 *)
let rec find_next_a_or_b a b = function
  | [] -> []
  | h :: t -> if h = a || h = b then h :: t else find_next_a_or_b a b t

(*
 *------------------------------------------------------------------------------
 * Generic function to determine in which relation two activities (a and b), 
 * are in a given list of activities.
 * Every time the current value is equal to the head of the list to check
 * traverse, the counter (cnt) is incremented by 1 and the a and b are 
 * switched. 
 * For every miss (h != cur), first the abortion_predicate is checked, if it is 
 * NOT fulfilled the find_next_function is applied to retrieve the next 
 * starting point of the list to continue check. Then the 
 * continuation_predicate is applied to the result of the list and if 
 * fulfilled, the search continous.
 * Finally, when the list is empty the found_predicate is checked and its 
 * result determines if the relation is fulfilled or not.
 *------------------------------------------------------------------------------
 * Input:
 * a ... activity
 * b ... activity
 * activities ... list of activities
 * abortion_predicate ... 
 *    a function to determine if the relation determination should be aborted. *    Expects cur, h, and t and applies the logic based on the DECLARE template
 *    definition of the specific relation
 * find_next_function ...
 *    a function to determine the next sub list to continue. Utilizes either
 *    find_next or find_next_a_or_b
 * continuation_predicate ...
 *    a function to determine if the relation determination should be continued
 *    after the find_next_function is applied. Expects next cur the recursive
 *    that should be applied in case of continuation wrapped in a function
 * found_predicate ...
 *    predicate to determine if the relation is true or false based on the cnt
 *    value
 *------------------------------------------------------------------------------
 *)
let is_relation (a : string) (b : string) (activities : string list)
    (abortion_predicate : string -> string -> string list -> bool)
    (find_next_function : string list -> string list)
    (continuation_predicate : string list -> string -> (unit -> bool) -> bool)
    (found_predicate : int -> bool) : bool =
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

(* Checks if a and b are fulfilling the succession declare constraint *)
let is_succession (a : string) (b : string) (activities : string list) =
  is_relation a b activities
    (fun cur h _t -> (cur = b && h = a) || (cur = a && h = b))
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

(* Checks if a and b are fulfilling the response declare constraint *)
let is_response (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur h _t -> cur = b && h = a)
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

(* Checks if a and b are fulfilling the precedence declare constraint *)
let is_precedence (a : string) (b : string) (activities : string list) : bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h = b)
    (fun t -> t)
    (fun _next _cur f -> f ())
    (fun cnt -> cnt >= 2)

(* 
 * Checks if a and b are fulfilling the alternate succession declare constraint
 *)
let is_alternate_succession (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun _cur h _t -> h = a || h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next cur f -> if List.hd next = cur then f () else false)
    (fun cnt -> cnt > 2)

(* Checks if a and b are fulfilling the alternate response declare constraint *)
let is_alternate_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b && h = a)
    (fun t -> find_next_a_or_b a b t)
    (fun next cur f ->
      let n = List.hd next in
      if not (cur = b && n = a) then f () else false)
    (fun cnt -> cnt > 2)

(* 
 * Checks if a and b are fulfilling the alternate precedence declare constraint
 *)
let is_alternate_precedence (a : string) (b : string) (activities : string list)
    : bool =
  is_relation a b activities
    (fun cur h _t -> cur = a && h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f ->
      let n = List.hd next in
      if n = b || (n = a && (List.tl next = [] || is_precedence a b next)) then
        f ()
      else false)
    (fun cnt -> cnt > 2)

(* Checks if a and b are fulfilling the chain succession declare constraint *)
let is_chain_succession (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> cur = b || h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)

(* Checks if a and b are fulfilling the chain response declare constraint *)
let is_chain_response (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur _h _t -> cur = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)

(* Checks if a and b are fulfilling the chain precedence declare constraint *)
let is_chain_precedence (a : string) (b : string) (activities : string list) :
    bool =
  is_relation a b activities
    (fun cur h _t -> a != b && cur = a && h = b)
    (fun t -> find_next_a_or_b a b t)
    (fun next _cur f -> if List.hd next = a then f () else false)
    (fun cnt -> cnt > 0 && cnt mod 2 = 0)
