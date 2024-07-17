open Amqp_client_async
open Thread

let host = "localhost"

let handler _channel message =
  let _content, data = message.Message.message in
  Log.info "Recieved message: %s" data;
  return ()

let consumer_cancelled () =
  Log.info "Consumer cancelled"

let _ =
  Connection.connect ~id:"otel-to-probdeclare-converter" host >>= fun connection ->
  Log.info "Connection started";
  Connection.open_channel ~id:"otel-to-probd-channel" Channel.no_confirm connection >>= fun channel ->
  Log.info "Channel opened";
  Queue.declare channel "otel-to-probd-queue" >>= fun queue ->
  Queue.consume ~id:"accept_traces" ~on_cancel:consumer_cancelled ~no_ack:true ~exclusive:true channel queue >>= fun (_consumer, reader) ->
  spawn (Pipe.iter reader ~f:(handler channel));
  Log.info "Listening for traces";
  return ()

let () =
  Scheduler.go ()
