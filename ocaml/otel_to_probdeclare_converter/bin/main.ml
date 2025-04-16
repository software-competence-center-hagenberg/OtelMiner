open Amqp_client_async
open Thread
open Util

let host = Sys.argv.(1)


(* 
 * Handles single trace, MUST NOT be called RESOURCE_SPANS or multiple traces!
 * Calling the function with RESOURCE_SPANS or multiple traces will lead to a 
 * fail!
 * type of data: { trace_id : string; spans : string list }
 *)
let handler_single_trace (tt : trace_type) channel probd_result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  let processed = Message_processor.process_trace tt data in
  Log.info "encoding result as string ...";
  (*let constraints = Declare.string_of_declare_list_list converted in*)
  let result = Message_processor.trace_model_to_json_string processed in
  Log.info "result: %s" result;
  Queue.publish channel probd_result_queue (Message.make result) >>= fun `Ok ->
  Log.info "Sent result to trace-receiver";
  return ()

(* 
 * Handles resource spans and multiple traces, should only be called with
 * RESOURCE_SPANS or multiple traces 
 * Calling the function with a single trace will lead to a fail!
 * type of data: { trace_ids : string list; spans : string list list }
 *)
let handler_resource_spans_and_multiple_traces (tt : trace_type) channel
    probd_result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  let processed = Message_processor.process_traces tt data in
  Log.info "encoding result as string ...";
  (*let constraints = Declare.string_of_declare_list_list converted in*)
  let result = Message_processor.traces_model_to_json_string processed in
  Log.info "result: %s" result;
  Queue.publish channel probd_result_queue (Message.make result) >>= fun `Ok ->
  Log.info "Sent result to trace-receiver";
  return ()

let rabbitmq_consumer_cancelled () = Log.info "Consumer cancelled"

(* 
 * sets up RabbitMQ Connections, opens up the channel, declares queues, 
 * and sets up consumers with the correct handler function
 *)
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
    (Pipe.iter reader
       ~f:
         (handler_resource_spans_and_multiple_traces RESOURCE_SPANS channel
            probd_result_queue));
  (* setting up listener for otel trace spans *)
  Queue.consume ~id:"accept-trace-spans" ~on_cancel:rabbitmq_consumer_cancelled
    ~no_ack:true ~exclusive:true channel trace_spans_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader
       ~f:(handler_single_trace OTEL_SPANS_LIST channel probd_result_queue));
  (* setting up listener for jaeger traces *)
  Queue.consume ~id:"accept-jaeger-traces"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader
       ~f:(handler_single_trace JAEGER_TRACE channel probd_result_queue));
  (* setting up listener for jaeger trace spans list *)
  Queue.consume ~id:"accept-jaeger-trace-spans"
    ~on_cancel:rabbitmq_consumer_cancelled ~no_ack:true ~exclusive:true channel
    jaeger_trace_spans_list_queue
  >>= fun (_consumer, reader) ->
  spawn
    (Pipe.iter reader
       ~f:(handler_single_trace JAEGER_SPANS_LIST channel probd_result_queue));
  Log.info "Listening for traces";
  (* TODO add listener for multiple traces *)
  return ()

let () =
  Log.info "RabbitMQ host name: %s" Sys.argv.(1);
  Scheduler.go ()
