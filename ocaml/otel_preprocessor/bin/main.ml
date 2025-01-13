open Amqp_client_async
open Thread
open Util

let host = Sys.argv.(1)

let handler (span : trace_string_type) channel result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  let processed = Message_processor.process span data in
  Log.info "encoding result as string ...";
  let result = Message_processor.result_to_json_string processed in
  Log.info "result: %s" result;
  Queue.publish channel result_queue 
    (Message.make result)
  >>= fun `Ok ->
  Log.info "Sent result to trace-receiver";
  return ()

let rabbitmq_consumer_cancelled () = Log.info "Consumer cancelled"

let _ =
  Connection.connect ~id:"otel-to-probdeclare-converter" host
  >>= fun connection ->
  Log.info "Connection started";
  Connection.open_channel ~id:"otel-preprocessing-channel" Channel.no_confirm
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
  Queue.declare channel "probd-result-queue" >>= fun result_queue ->
  Log.info "Created result queue";
  (* setting up listener for resource spans *)
  Queue.consume ~id:"accept-resource-spans"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    resource_spans_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler RESOURCE_SPANS channel result_queue));
  (* setting up listener for otel trace spans *)
  Queue.consume ~id:"accept-trace-spans" ~on_cancel:rabbitmq_consumer_cancelled
    ~no_ack:true ~exclusive:true channel trace_spans_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler OTEL_SPANS_LIST channel result_queue));
  (* setting up listener for jaeger traces *)
  Queue.consume ~id:"accept-jaeger-traces"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_queue
  >>= fun (_consumer, reader) ->
  spawn (Pipe.iter reader ~f:(handler JAEGER_TRACE channel result_queue));
  (* setting up listener for jaeger trace spans list *)
  Queue.consume ~id:"accept-jaeger-trace-spans"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_spans_list_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader ~f:(handler JAEGER_SPANS_LIST channel result_queue));
  Log.info "Listening for traces";
  return ()

let () =
  Log.info "RabbitMQ host name: %s" Sys.argv.(1);
  Scheduler.go ()
