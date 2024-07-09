#FROM ubuntu:24.10
FROM golang:1.23rc1-bookworm AS build

WORKDIR /otel

COPY --chmod=755 . .

RUN ./ocb --config builder-config.yaml

FROM golang:1.23rc1-bookworm

COPY --from=build /otel /otel

ENTRYPOINT ["/otel/otelcol-dev/otelcol-dev"]
CMD ["--config", "/otel/config.yaml"]