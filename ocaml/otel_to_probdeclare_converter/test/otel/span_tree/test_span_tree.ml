open Otel_decoder
open OUnit2
open Span_tree

let jaeger_trace_span_tree =
  {
    span =
      {
        trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
        span_id = Bytes.of_string "26562d296681ec96";
        parent_span_id = Bytes.empty;
        name = "POST /travel/getTripsByRouteId";
        kind = Span_kind_unspecified;
        start_time_unix_nano = 1716599841373000L;
        end_time_unix_nano = 1716599841381803L;
        attributes = [];
        dropped_attributes_count = 0l;
        events = [];
        dropped_events_count = 0l;
        links = [];
        dropped_links_count = 0l;
        status = None;
        trace_state = "";
      };
    children =
      [
        {
          span =
            {
              trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
              span_id = Bytes.of_string "eb325ba2f880cdeb";
              parent_span_id = Bytes.of_string "26562d296681ec96";
              name = "TravelController.getTripsByRouteId";
              kind = Span_kind_unspecified;
              start_time_unix_nano = 1716599841378809L;
              end_time_unix_nano = 1716599841381531L;
              attributes = [];
              dropped_attributes_count = 0l;
              events = [];
              dropped_events_count = 0l;
              links = [];
              dropped_links_count = 0l;
              status = None;
              trace_state = "";
            };
          children =
            [
              {
                span =
                  {
                    trace_id =
                      Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
                    span_id = Bytes.of_string "1c63fae4589d3097";
                    parent_span_id = Bytes.of_string "eb325ba2f880cdeb";
                    name = "TripRepository.findByRouteId";
                    kind = Span_kind_unspecified;
                    start_time_unix_nano = 1716599841380375L;
                    end_time_unix_nano = 1716599841381014L;
                    attributes = [];
                    dropped_attributes_count = 0l;
                    events = [];
                    dropped_events_count = 0l;
                    links = [];
                    dropped_links_count = 0l;
                    status = None;
                    trace_state = "";
                  };
                children =
                  [
                    {
                      span =
                        {
                          trace_id =
                            Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
                          span_id = Bytes.of_string "8c91642e5d12dbb7";
                          parent_span_id = Bytes.of_string "1c63fae4589d3097";
                          name = "find ts.trip";
                          kind = Span_kind_unspecified;
                          start_time_unix_nano = 1716599841380659L;
                          end_time_unix_nano = 1716599841380960L;
                          attributes = [];
                          dropped_attributes_count = 0l;
                          events = [];
                          dropped_events_count = 0l;
                          links = [];
                          dropped_links_count = 0l;
                          status = None;
                          trace_state = "";
                        };
                      children = [];
                    };
                  ];
              };
              {
                span =
                  {
                    trace_id =
                      Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
                    span_id = Bytes.of_string "929b185b128eee15";
                    parent_span_id = Bytes.of_string "eb325ba2f880cdeb";
                    name = "TripRepository.findByRouteId";
                    kind = Span_kind_unspecified;
                    start_time_unix_nano = 1716599841379346L;
                    end_time_unix_nano = 1716599841380341L;
                    attributes = [];
                    dropped_attributes_count = 0l;
                    events = [];
                    dropped_events_count = 0l;
                    links = [];
                    dropped_links_count = 0l;
                    status = None;
                    trace_state = "";
                  };
                children =
                  [
                    {
                      span =
                        {
                          trace_id =
                            Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
                          span_id = Bytes.of_string "6fbaa2ec09738479";
                          parent_span_id = Bytes.of_string "929b185b128eee15";
                          name = "find ts.trip";
                          kind = Span_kind_unspecified;
                          start_time_unix_nano = 1716599841379836L;
                          end_time_unix_nano = 1716599841380274L;
                          attributes = [];
                          dropped_attributes_count = 0l;
                          events = [];
                          dropped_events_count = 0l;
                          links = [];
                          dropped_links_count = 0l;
                          status = None;
                          trace_state = "";
                        };
                      children = [];
                    };
                  ];
              };
            ];
        };
      ];
  }

let jaeger_5575e1e883a056898c9ddee917664f9a_span_tree =
  {
    span =
      {
        trace_id = Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
        span_id = Bytes.of_string "597f20666951566b";
        parent_span_id = Bytes.empty;
        name = "POST /travel/create";
        kind = Span_kind_unspecified;
        start_time_unix_nano = 1716598946827000L;
        end_time_unix_nano = 1716598946837313L;
        attributes = [];
        dropped_attributes_count = 0l;
        events = [];
        dropped_events_count = 0l;
        links = [];
        dropped_links_count = 0l;
        status = None;
        trace_state = "";
      };
    children =
      [
        {
          span =
            {
              trace_id = Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
              span_id = Bytes.of_string "208d1e7a2166808d";
              parent_span_id = Bytes.of_string "597f20666951566b";
              name = "TravelController.create";
              kind = Span_kind_unspecified;
              start_time_unix_nano = 1716598946828828L;
              end_time_unix_nano = 1716598946833733L;
              attributes = [];
              dropped_attributes_count = 0l;
              events = [];
              dropped_events_count = 0l;
              links = [];
              dropped_links_count = 0l;
              status = None;
              trace_state = "";
            };
          children =
            [
              {
                span =
                  {
                    trace_id =
                      Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
                    span_id = Bytes.of_string "aa33fe635d1613d6";
                    parent_span_id = Bytes.of_string "208d1e7a2166808d";
                    name = "TripRepository.findByTripId";
                    kind = Span_kind_unspecified;
                    start_time_unix_nano = 1716598946830004L;
                    end_time_unix_nano = 1716598946831855L;
                    attributes = [];
                    dropped_attributes_count = 0l;
                    events = [];
                    dropped_events_count = 0l;
                    links = [];
                    dropped_links_count = 0l;
                    status = None;
                    trace_state = "";
                  };
                children =
                  [
                    {
                      span =
                        {
                          trace_id =
                            Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
                          span_id = Bytes.of_string "17d26a938bce4150";
                          parent_span_id = Bytes.of_string "aa33fe635d1613d6";
                          name = "find ts.trip";
                          kind = Span_kind_unspecified;
                          start_time_unix_nano = 1716598946830969L;
                          end_time_unix_nano = 1716598946831689L;
                          attributes = [];
                          dropped_attributes_count = 0l;
                          events = [];
                          dropped_events_count = 0l;
                          links = [];
                          dropped_links_count = 0l;
                          status = None;
                          trace_state = "";
                        };
                      children = [];
                    };
                  ];
              };
              {
                span =
                  {
                    trace_id =
                      Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
                    span_id = Bytes.of_string "f28e007ee0508339";
                    parent_span_id = Bytes.of_string "208d1e7a2166808d";
                    name = "TripRepository.save";
                    kind = Span_kind_unspecified;
                    start_time_unix_nano = 1716598946831960L;
                    end_time_unix_nano = 1716598946833564L;
                    attributes = [];
                    dropped_attributes_count = 0l;
                    events = [];
                    dropped_events_count = 0l;
                    links = [];
                    dropped_links_count = 0l;
                    status = None;
                    trace_state = "";
                  };
                children =
                  [
                    {
                      span =
                        {
                          trace_id =
                            Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
                          span_id = Bytes.of_string "c974774d399a2734";
                          parent_span_id = Bytes.of_string "f28e007ee0508339";
                          name = "update ts.trip";
                          kind = Span_kind_unspecified;
                          start_time_unix_nano = 1716598946832828L;
                          end_time_unix_nano = 1716598946833365L;
                          attributes = [];
                          dropped_attributes_count = 0l;
                          events = [];
                          dropped_events_count = 0l;
                          links = [];
                          dropped_links_count = 0l;
                          status = None;
                          trace_state = "";
                        };
                      children = [];
                    };
                  ];
              };
            ];
        };
        {
          span =
            {
              trace_id = Bytes.of_string "5575e1e883a056898c9ddee917664f9a";
              span_id = Bytes.of_string "ab123625f5654aeb";
              parent_span_id = Bytes.of_string "597f20666951566b";
              name = "BasicErrorController.error";
              kind = Span_kind_unspecified;
              start_time_unix_nano = 1716598946836292L;
              end_time_unix_nano = 1716598946836990L;
              attributes = [];
              dropped_attributes_count = 0l;
              events = [];
              dropped_events_count = 0l;
              links = [];
              dropped_links_count = 0l;
              status = None;
              trace_state = "";
            };
          children = [];
        };
      ];
  }

let test_generate_span_trees_from_spans _ =
  let json = Util.load_json_from_file "ac31d6a4e6fab4b650a501274d48d3c5.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let _ = generate_span_tree_from_spans_for_single_trace decoded in
  Format.fprintf Format.std_formatter
    "otel-trace ac31d6a4e6fab4b650a501274d48d3c5: successfully generated \
     span_tree list\n";
  assert_bool "" true;
  let json = Util.load_json_from_file "jaeger_trace.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let span_trees = generate_span_tree_from_spans_for_single_trace decoded in
  assert_equal jaeger_trace_span_tree span_trees;
  let json =
    Util.load_json_from_file "jaeger_5575e1e883a056898c9ddee917664f9a.json"
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let span_trees = generate_span_tree_from_spans_for_single_trace decoded in
  assert_equal jaeger_5575e1e883a056898c9ddee917664f9a_span_tree span_trees
(* FIXME make more compact *)

let jaeger_trace_root =
  Some
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "26562d296681ec96";
          parent_span_id = Bytes.empty;
          name = "POST /travel/getTripsByRouteId";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841373000L;
          end_time_unix_nano = 1716599841381803L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    }

let jaeger_trace_nodes =
  [
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "6fbaa2ec09738479";
          parent_span_id = Bytes.of_string "929b185b128eee15";
          name = "find ts.trip";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841379836L;
          end_time_unix_nano = 1716599841380274L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    };
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "1c63fae4589d3097";
          parent_span_id = Bytes.of_string "eb325ba2f880cdeb";
          name = "TripRepository.findByRouteId";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841380375L;
          end_time_unix_nano = 1716599841381014L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    };
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "929b185b128eee15";
          parent_span_id = Bytes.of_string "eb325ba2f880cdeb";
          name = "TripRepository.findByRouteId";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841379346L;
          end_time_unix_nano = 1716599841380341L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    };
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "8c91642e5d12dbb7";
          parent_span_id = Bytes.of_string "1c63fae4589d3097";
          name = "find ts.trip";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841380659L;
          end_time_unix_nano = 1716599841380960L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    };
    {
      span =
        {
          trace_id = Bytes.of_string "52fcf556bf86dac4f5866b8b50a81031";
          span_id = Bytes.of_string "eb325ba2f880cdeb";
          parent_span_id = Bytes.of_string "26562d296681ec96";
          name = "TravelController.getTripsByRouteId";
          kind = Span_kind_unspecified;
          start_time_unix_nano = 1716599841378809L;
          end_time_unix_nano = 1716599841381531L;
          attributes = [];
          dropped_attributes_count = 0l;
          events = [];
          dropped_events_count = 0l;
          links = [];
          dropped_links_count = 0l;
          status = None;
          trace_state = "";
        };
      children = [];
    };
  ]

(*
roots:
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = 597f20666951566b; parent_span_id = ; name = POST /travel/create; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946827000; end_time_unix_nano = 1716598946837313; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
nodes:
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = 17d26a938bce4150; parent_span_id = aa33fe635d1613d6; name = find ts.trip; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946830969; end_time_unix_nano = 1716598946831689; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = aa33fe635d1613d6; parent_span_id = 208d1e7a2166808d; name = TripRepository.findByTripId; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946830004; end_time_unix_nano = 1716598946831855; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = c974774d399a2734; parent_span_id = f28e007ee0508339; name = update ts.trip; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946832828; end_time_unix_nano = 1716598946833365; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = f28e007ee0508339; parent_span_id = 208d1e7a2166808d; name = TripRepository.save; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946831960; end_time_unix_nano = 1716598946833564; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = 208d1e7a2166808d; parent_span_id = 597f20666951566b; name = TravelController.create; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946828828; end_time_unix_nano = 1716598946833733; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
{
    span = { trace_id = 5575e1e883a056898c9ddee917664f9a; span_id = ab123625f5654aeb; parent_span_id = 597f20666951566b; name = BasicErrorController.error; kind = Span_kind_unspecified; start_time_unix_nano = 1716598946836292; end_time_unix_nano = 1716598946836990; attributes = []; dropped_attributes_count = 0; events = []; dropped_events_count = 0; links = []; dropped_links_count = 0; status =  }; 
    children = [] 
}
*)
let test_generate_nodes _ =
  let test_aux json expected_root expected_nodes =
    let spans = json |> Yojson.Basic.Util.to_list in
    let decoded = List.map decode_jaeger_trace_span spans in
    let roots, nodes = generate_nodes_for_single_trace decoded in
    assert_equal expected_root roots;
    assert_equal expected_nodes nodes
  in
  let json = Util.load_json_from_file "ac31d6a4e6fab4b650a501274d48d3c5.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let _, _ = generate_nodes_for_single_trace decoded in
  Format.fprintf Format.std_formatter
    "otel-trace ac31d6a4e6fab4b650a501274d48d3c5: successfully generated root \
     and nodes\n";
  assert_bool "" true;
  let json = Util.load_json_from_file "jaeger_trace.json" in
  test_aux json jaeger_trace_root jaeger_trace_nodes;
  let json =
    Util.load_json_from_file "jaeger_5575e1e883a056898c9ddee917664f9a.json"
  in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let root, nodes = generate_nodes_for_single_trace decoded in
  Format.fprintf Format.std_formatter
    "jaeger_5575e1e883a056898c9ddee917664f9a: successfully generated roots and \
     nodes\n";
  Format.fprintf Format.std_formatter "roots:\n";
  match root with
  | Some r ->
      pp_span_tree Format.std_formatter r;
      Format.fprintf Format.std_formatter "nodes:\n";
      List.iter (pp_span_tree Format.std_formatter) nodes;
      assert_bool "" true
  | None -> assert_bool "" false

let suite =
  "span_tree_test_suite"
  >::: [
         "test_generate_span_trees_from_spans"
         >:: test_generate_span_trees_from_spans;
         "test_generate_nodes" >:: test_generate_nodes;
       ]

let () = run_test_tt_main suite
