open Otel_decoder
open OUnit2
open Span_tree

let jaeger_trace_span_trees =
  [
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
    };
  ]

let test_generate_span_trees_from_spans _ =
  let json = Util.load_json_from_file "ac31d6a4e6fab4b650a501274d48d3c5.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let _ = generate_span_trees_from_spans decoded in
  Format.fprintf Format.std_formatter
    "otel-trace ac31d6a4e6fab4b650a501274d48d3c5: successfully generated \
     span_tree list\n";
  assert_bool "" true;
  let json = Util.load_json_from_file "jaeger_trace.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let span_trees = generate_span_trees_from_spans decoded in
  assert_equal jaeger_trace_span_trees span_trees

let jaeger_trace_roots =
  [
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
    };
  ]

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

let test_generate_nodes _ =
  let json = Util.load_json_from_file "ac31d6a4e6fab4b650a501274d48d3c5.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_trace_span spans in
  let _, _ = generate_nodes decoded in
  Format.fprintf Format.std_formatter
    "otel-trace ac31d6a4e6fab4b650a501274d48d3c5: successfully generated roots \
     and nodes\n";
  assert_bool "" true;
  let json = Util.load_json_from_file "jaeger_trace.json" in
  let spans = json |> Yojson.Basic.Util.to_list in
  let decoded = List.map decode_jaeger_trace_span spans in
  let roots, nodes = generate_nodes decoded in
  assert_equal jaeger_trace_roots roots;
  assert_equal jaeger_trace_nodes nodes

let suite =
  "span_tree_test_suite"
  >::: [
         "test_generate_span_trees_from_spans"
         >:: test_generate_span_trees_from_spans;
         "test_generate_nodes" >:: test_generate_nodes;
       ]

let () = run_test_tt_main suite
