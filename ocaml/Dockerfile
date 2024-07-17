FROM ocaml/opam:debian-12-ocaml-4.14-afl

COPY --chmod=750 ./otel_to_probdeclare_converter /otel_to_probdeclare_converter

RUN cd /otel_to_probdeclare_converter
RUN opam install dune
RUN eval $(opam env)
RUN opam install amqp-client-async
RUN dune build

ENTRYPOINT ["dune", "exec"]
