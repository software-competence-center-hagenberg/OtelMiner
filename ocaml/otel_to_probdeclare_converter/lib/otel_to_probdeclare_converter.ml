open Opentelemetry_proto

module type Otel_to_ProbDeclare_Converter = sig
  type ltl

  val convert : Trace.resource_spans list -> ltl list
  val get_ltl_string : Trace.resource_spans list -> string
end

module Converter: Otel_to_ProbDeclare_Converter = struct
  (*
   * V ... value 
   * G ... globally
   * F ... eventually
   * U ... until
   * N ... next
   * Note that there is no weak until -> instead use OR (U(a,b), V (a))
   *)
   type ltl =
   V of string |
   G of ltl |
   F of ltl |
   N of ltl |
   NOT of ltl |
   U of ltl * ltl |
   AND of ltl * ltl |
   OR of ltl * ltl

   let rec string_of_ltl t =
    match t with
    | V s -> s
    | G t0 -> "G" ^ string_of_ltl t0
    | F t0 -> "F" ^ string_of_ltl t0
    | N t0 -> "N" ^ string_of_ltl t0
    | NOT t0 -> "NOT" ^ string_of_ltl t0
    | U (t0, t1) -> "U(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
    | AND (t0, t1) -> "AND(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"
    | OR (t0, t1) -> "OR(" ^ string_of_ltl t0 ^ ", " ^ string_of_ltl t1 ^ ")"

  let map_to_ltl (rs : Trace.resource_spans): ltl =
    let l = List.length rs.scope_spans in
      if l > 1 then V "a" 
      else if l > 2 then G (V "b")
      else if l > 3 then F (V "a")
      else if l > 4 then N (V "a")
      else if l > 5 then NOT (V "b")
      else if l > 6 then U (V "a", V "b")
      else if l > 7 then AND (V "a", V "b")
      else OR (V "a", V "b")
    
  let convert (resource_spans : Trace.resource_spans list) : ltl list = 
    let rec convert_aux l k =
      match l with
      | [] -> k []
      | h :: t -> convert_aux t (fun a -> k ((map_to_ltl h):: a))
  in convert_aux resource_spans (fun x -> x)

  let get_ltl_string (resource_spans : Trace.resource_spans list) : string =
    let ltls = convert resource_spans in
    let rec get_ltl_string_aux ltls acc=
      match ltls with
      | [] -> acc
      | h :: t -> get_ltl_string_aux t (acc ^ ", " ^ string_of_ltl h)
    in get_ltl_string_aux ltls ""


end