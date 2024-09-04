(*open Yojson.Basic*)
open Opentelemetry_proto

(* Custom pretty-print function for spans *)
let pp_span_custom (fmt : Format.formatter) (span : Trace.span) =
  let trace_id = Bytes.to_string span.trace_id in
  let span_id = Bytes.to_string span.span_id in
  let parent_span_id = Bytes.to_string span.parent_span_id in
  let pp_attributes fmt attrs =
    Format.fprintf fmt "[%a]"
      (Format.pp_print_list
         ~pp_sep:(fun fmt () -> Format.fprintf fmt "; ")
         Common.pp_key_value)
      attrs
  in
  let pp_events fmt events =
    Format.fprintf fmt "[%a]"
      (Format.pp_print_list
         ~pp_sep:(fun fmt () -> Format.fprintf fmt "; ")
         Trace.pp_span_event)
      events
  in
  let pp_links fmt links =
    Format.fprintf fmt "[%a]"
      (Format.pp_print_list
         ~pp_sep:(fun fmt () -> Format.fprintf fmt "; ")
         Trace.pp_span_link)
      links
  in
  Format.fprintf fmt
    "{ trace_id = %s; span_id = %s; parent_span_id = %s; name = %s; kind = %a; \
     start_time_unix_nano = %Ld; end_time_unix_nano = %Ld; attributes = %a; \
     dropped_attributes_count = %ld; events = %a; dropped_events_count = %ld; \
     links = %a; dropped_links_count = %ld; status = %a }"
    trace_id span_id parent_span_id span.name Trace.pp_span_span_kind span.kind
    span.start_time_unix_nano span.end_time_unix_nano pp_attributes
    span.attributes span.dropped_attributes_count pp_events span.events
    span.dropped_events_count pp_links span.links span.dropped_links_count
    (Format.pp_print_option Trace.pp_status)
    span.status

let pp_span_custom_minimal (fmt : Format.formatter) (span : Trace.span) =
  let trace_id = Bytes.to_string span.trace_id in
  let span_id = Bytes.to_string span.span_id in
  let parent_span_id = Bytes.to_string span.parent_span_id in
  Format.fprintf fmt
    "{ \n    trace_id = %s; \n    span_id = %s; \n    parent_span_id = %s \n  }\n"
    trace_id span_id parent_span_id
