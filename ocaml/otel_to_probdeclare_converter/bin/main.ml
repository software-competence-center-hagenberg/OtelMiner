open Amqp_client_async
open Thread
open Otel_to_prob_declare_converter

let host = Sys.argv.(1)

let handler channel probd_result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  Log.info "decoding ...";
  let decoded = Otel_decoder.decode data in
  Log.info "decoding complete";
  Log.info "converting ...";
  let converted = convert decoded in
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
  Queue.declare channel "otel-to-probd-queue" >>= fun otel_to_probd_queue ->
  Log.info "Created listener queue";
  Queue.declare channel "probd-result-queue" >>= fun probd_result_queue ->
  Log.info "Created result queue";
  Queue.consume ~id:"accept_traces" ~on_cancel:rabbitmq_consumer_cancelled
    ~no_ack:true ~exclusive:true channel otel_to_probd_queue
  >>= fun (_consumer, reader) ->
  spawn (Pipe.iter reader ~f:(handler channel probd_result_queue));
  Log.info "Listening for traces";
  return ()

let () =
  Log.info "RabbitMQ host name: %s" Sys.argv.(1);
  Scheduler.go ()
