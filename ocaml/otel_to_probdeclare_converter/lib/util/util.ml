module DeclareSet = Set.Make (Declare)
module StringSet = Set.Make (String)

let load_json_from_file ?(prefix : string = "/../../../../../test/")
    (file_name : string) : Yojson.Basic.t =
  Yojson.Basic.from_file (Sys.getcwd () ^ prefix ^ file_name)
