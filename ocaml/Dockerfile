# Use an OCaml and opam ready image
FROM ocaml/opam:debian-12-ocaml-4.14-afl

# Copy the local project to the Docker image
COPY --chown=opam ./otel_to_probdeclare_converter /home/opam/otel_to_probdeclare_converter

# Set the working directory
WORKDIR /home/opam/otel_to_probdeclare_converter

# Install dune and amqp-client-async, and build the project
RUN opam install dune amqp-client-async opentelemetry && \
    eval $(opam env) && \
    dune build

# execute the program in a shell
CMD eval $(opam env) && dune exec otel_to_probdeclare_converter rabbitmq-server
