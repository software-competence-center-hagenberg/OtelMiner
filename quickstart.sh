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
    -j          Enable spring boot processes
    -f          Enable frontend
EOF
}

# Initialize variables
BUILD=false
OCAML=false
SPRING=false
FRONTEND=false

# Parse command-line options
while getopts "hbaojf" opt; do
    case ${opt} in
        h) show_help; exit 0 ;;
        b) BUILD=true ;;
        a) OCAML=true; SPRING=true; FRONTEND=true ;;
        o) OCAML=true ;;
        j) SPRING=true ;;
        f) FRONTEND=true ;;
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
[ "$FRONTEND" = true ] && start_service frontend

exit 0