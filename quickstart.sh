#!/usr/bin/env bash

# Function to display help message
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Options:
    -h         Display this help message
    -b         Trigger build
    -a         Enable all processes
    -o         Enable ocaml process
    -j         Enable all java processes
    -d         Enable db-service
    -r         Enable trace-receiver
    -t         Enable all typescript processes.
    -e         Enable all backend processes.
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
while getopts "hbaojdrte" opt; do
    case ${opt} in
        h) show_help; exit 0 ;;
        b) echo "enabling build flag..."; BUILD=true ;;
        a) echo "enabling all processes..."; OCAML=true; SPRING=true; TS=true ;;
        o) echo "enabling ocaml process..."; OCAML=true ;;
        j) echo "enabling all java processes..."; SPRING=true ;;
        d) echo "enabling db-service..."; DB_SERVICE=true ;;
        r) echo "enabling trace-receiver..."; TRACE_RECEIVER=true ;;
        t) echo "enabling all typescript processes..."; TS=true ;;
        e) echo "enabling all backend processes..."; OCAML=true; SPRING=true ;;
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