open Amqp_client_async
open Thread
open Otel_to_prob_declare_converter
open Util

let host = Sys.argv.(1)

let convert (span : trace_string_type) (data : string) =
  match span with
  | JAEGER_TRACE ->
      let decoded = Otel_decoder.decode_jaeger_trace_string data in
      convert_trace_spans decoded
  | JAEGER_SPANS_LIST ->
      let decoded = Otel_decoder.decode_jaeger_spans_list_string data in
      convert_trace_spans decoded
  | OTEL_SPANS_LIST ->
      let decoded = Otel_decoder.decode_trace_string data in
      convert_trace_spans decoded
  | RESOURCE_SPANS ->
      let decoded = Otel_decoder.decode_resources_spans_string data in
      Log.info "decoding complete";
      Log.info "converting ...";
      convert_resource_spans decoded

let handler (span : trace_string_type) channel probd_result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  Log.info "decoding ...";
  let converted = convert span data in
  Log.info "conversion complete";
  Log.info "encoding result as string ...";
  let ltl_string = Declare.string_of_declare_list_list converted in
  Log.info "encoding complete";
  Queue.publish channel probd_result_queue (Message.make ltl_string)
  >>= fun `Ok ->
  Log.info "Sent result to trace-receiver";
  return ()

let rabbitmq_consumer_cancelled () = Log.info "Consumer cancelled"

let _ =
  Connection.connect ~id:"otel-to-probdeclare-converter" host
  >>= fun connection ->
  Log.info "Connection started";
  Connection.open_channel ~id:"otel-to-probd-channel" Channel.no_confirm
    connection
  >>= fun channel ->
  Log.info "Channel opened";
  Queue.declare channel "resource-spans-queue" >>= fun resource_spans_queue ->
  Log.info "Created resource spans listener queue";
  Queue.declare channel "trace-spans-queue" >>= fun trace_spans_queue ->
  Log.info "Created trace spans listener queue";
  Queue.declare channel "jaeger-trace-queue" >>= fun jaeger_trace_queue ->
  Log.info "Created jaeger trace listener queue";
  Queue.declare channel "jaeger-trace-spans-list-queue"
  >>= fun jaeger_trace_spans_list_queue ->
  Log.info "Created jaeger trace spans list listener queue";
  Queue.declare channel "probd-result-queue" >>= fun probd_result_queue ->
  Log.info "Created result queue";
  (* setting up listener for resource spans *)
  Queue.consume ~id:"accept-resource-spans"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    resource_spans_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler RESOURCE_SPANS channel probd_result_queue));
  (* setting up listener for otel trace spans *)
  Queue.consume ~id:"accept-trace-spans" ~on_cancel:rabbitmq_consumer_cancelled
    ~no_ack:true ~exclusive:true channel trace_spans_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler OTEL_SPANS_LIST channel probd_result_queue));
  (* setting up listener for jaeger traces *)
  Queue.consume ~id:"accept-jaeger-traces"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_queue
  >>= fun (_consumer, reader) ->
  spawn (Pipe.iter reader ~f:(handler JAEGER_TRACE channel probd_result_queue));
  (* setting up listener for jaeger trace spans list *)
  Queue.consume ~id:"accept-jaeger-trace-spans"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_spans_list_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler JAEGER_SPANS_LIST channel probd_result_queue));
  Log.info "Listening for traces";
  return ()

let () =
  Log.info "RabbitMQ host name: %s" Sys.argv.(1);
  Scheduler.go ()
