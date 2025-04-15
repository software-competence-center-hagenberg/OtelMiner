#!/usr/bin/env bash

# Function to display help message
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Options:
    -h          Display this help message
    -b          Trigger build
    -a          Enable all processes
    -o          Enable ocaml process
    -j          Enable all java processes
    -d         Enable db-service
    -r         Enable trace-receiver
    -t          Enable all typescript processes
EOF
}

# Initialize variables
BUILD=false
OCAML=false
SPRING=false
DB_SERVICE=false
TRACE_RECEIVER=false
TS=false

# Parse command-line options
while getopts "hbaojdrt" opt; do
    case ${opt} in
        h) show_help; exit 0 ;;
        b) BUILD=true ;;
        a) OCAML=true; SPRING=true; TS=true ;;
        o) OCAML=true ;;
        j) SPRING=true ;;
        d) DB_SERVICE=true ;;
        r) TRACE_RECEIVER=true ;;
        t) TS=true ;;
        *) echo "Invalid option: -$OPTARG" >&2; show_help; exit 1 ;;
    esac
done

# Start core services
echo "Starting services from third party images..."
docker compose up --detach rabbitmq-server database

# Function to start a service with or without build
start_service() {
    local service=$1
    local build_flag=""

    # Add build flag if building is enabled
    [ "$BUILD" = true ] && build_flag="--build"

    docker compose up --detach $build_flag $service
}

# Start services based on flags
[ "$OCAML" = true ] && start_service otel-to-probdeclare-converter
[ "$SPRING" = true ] && start_service "db-service trace-receiver"
[ "$DB_SERVICE" = true ] && start_service db-service
[ "$TRACE_RECEIVER" = true ] && start_service trace-receiver
[ "$TS" = true ] && start_service frontend

exit 0