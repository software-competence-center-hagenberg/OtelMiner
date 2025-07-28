#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $0 <json_file>"
    echo "Example: $0 data.json"
    exit 1
fi

JSON_FILE="$1"

# Check if file exists
if [ ! -f "$JSON_FILE" ]; then
    echo "Error: File '$JSON_FILE' not found!"
    exit 1
fi

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is not installed. Please install jq to run this script."
    echo "On Ubuntu/Debian: sudo apt-get install jq"
    echo "On macOS: brew install jq"
    exit 1
fi

# Extract start_time values and find min/max
echo "Analyzing start_time values in $JSON_FILE..."
echo

# Get all start_time values
START_TIMES=$(jq -r '.records[].start_time' "$JSON_FILE" 2>/dev/null)

if [ $? -ne 0 ] || [ -z "$START_TIMES" ]; then
    echo "Error: Could not extract start_time values from JSON file."
    echo "Please ensure the file has the correct structure: {\"records\": [{\"start_time\": \"...\", ...}, ...]}"
    exit 1
fi

# Count total records
TOTAL_RECORDS=$(echo "$START_TIMES" | wc -l)
echo "Total records found: $TOTAL_RECORDS"

# Find min and max values
MIN_TIME=$(echo "$START_TIMES" | sort | head -n1)
MAX_TIME=$(echo "$START_TIMES" | sort | tail -n1)

echo "Minimum start_time: $MIN_TIME"
echo "Maximum start_time: $MAX_TIME"

# Optional: Show time difference if times are in a recognizable format
if date -d "$MIN_TIME" &>/dev/null && date -d "$MAX_TIME" &>/dev/null; then
    echo
    echo "Time span analysis:"
    MIN_EPOCH=$(date -d "$MIN_TIME" +%s)
    MAX_EPOCH=$(date -d "$MAX_TIME" +%s)
    DIFF_SECONDS=$((MAX_EPOCH - MIN_EPOCH))
    
    if [ $DIFF_SECONDS -gt 0 ]; then
        DAYS=$((DIFF_SECONDS / 86400))
        HOURS=$(((DIFF_SECONDS % 86400) / 3600))
        MINUTES=$(((DIFF_SECONDS % 3600) / 60))
        
        echo "Time difference: ${DAYS} days, ${HOURS} hours, ${MINUTES} minutes"
    fi
fi
