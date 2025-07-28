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
    -r         Enable model-generator
    -t         Enable all typescript processes.
    -e         Enable all backend processes.
EOF
}

# Initialize variables
BUILD=false
OCAML=false
SPRING=false
DB_SERVICE=false
MODEL_GENERATOR=false
TS=false
DETACH=true

# Parse command-line options
while getopts "hbaojdrte" opt; do
    case ${opt} in
        h) show_help; exit 0 ;;
        b) echo "enabling build flag..."; BUILD=true ;;
        a) echo "enabling all processes..."; OCAML=true; SPRING=true; TS=true ;;
        o) echo "enabling ocaml process..."; OCAML=true ;;
        j) echo "enabling all java processes..."; SPRING=true ;;
        d) echo "enabling db-service..."; DB_SERVICE=true ;;
        r) echo "enabling model-generator..."; MODEL_GENERATOR=true ;;
        t) echo "enabling all typescript processes..."; TS=true ;;
        e) echo "enabling all backend processes..."; OCAML=true; SPRING=true ;;
        l) echo "log enabled -> not detaching..."; DETACH=false ;;
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
    local detach_flag=""

    # Add build flag if building is enabled
    [ "$BUILD" = true ] && build_flag="--build"
    [ "$DETACH" = true ] && detach_flag="--detach"

    docker compose up $detach_flag $build_flag $service
}

# Start services based on flags
[ "$OCAML" = true ] && start_service otel-to-declare-converter
[ "$SPRING" = true ] && start_service "db-service model-generator"
[ "$DB_SERVICE" = true ] && start_service db-service
[ "$TRACE_RECEIVER" = true ] && start_service model-generator
[ "$TS" = true ] && start_service dashboard

exit 0