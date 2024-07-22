open Amqp_client_async
open Thread

let host = Sys.argv.(1)

let handler channel probd_result_queue message =
  let _content, data = message.Message.message in
  Log.info "Received message: %s" data;
  Queue.publish channel probd_result_queue (Message.make data) >>= fun `Ok ->
  Log.info "Sent result to trace-receiver";
  return ()

let consumer_cancelled () =
  Log.info "Consumer cancelled"

let _ =
  Connection.connect ~id:"otel-to-probdeclare-converter" host >>= fun connection ->
  Log.info "Connection started";
  Connection.open_channel ~id:"otel-to-probd-channel" Channel.no_confirm connection >>= fun channel ->
  Log.info "Channel opened";
  Queue.declare channel "otel-to-probd-queue" >>= fun otel_to_probd_queue ->
  Log.info "Created listener queue";
  Queue.declare channel "probd-result-queue" >>= fun probd_result_queue ->
  Log.info "Created result queue";
  Queue.consume ~id:"accept_traces" ~on_cancel:consumer_cancelled ~no_ack:true ~exclusive:true channel otel_to_probd_queue >>= fun (_consumer, reader) ->
  spawn (Pipe.iter reader ~f:(handler channel probd_result_queue));
  Log.info "Listening for traces";
  return ()

let () =
  Log.info "RabbitMQ host name: %s" Sys.argv.(1);
  Scheduler.go ()
