#!/bin/bash

# Function to extract data from a single file
parse_file() {
    local file="$1"
    local filename=$(basename "$file")

    # Initialize variables
    local minor_changes=""
    local major_changes=""
    local s1_only=""
    local s2_only=""

    # Extract minor changes (< 0.1)
    minor_changes=$(grep -E "Common constraints minor changes.*< 0\.1.*:" "$file" | sed -E 's/.*< 0\.1[^:]*: *([0-9]+).*/\1/')

    # Extract major changes (> 0.1)
    major_changes=$(grep -E "Common constraints changed.*> 0\.1.*:" "$file" | sed -E 's/.*> 0\.1[^:]*: *([0-9]+).*/\1/')

    # Get all S*.json constraint lines
    local s_lines=$(grep -E "file S[0-9]+(_only)?\.json contains [0-9]+ constraints that only occur here" "$file")
    local line_count=$(echo "$s_lines" | grep -c .)

    # Extract S1 only constraints (first S*.json file mentioned)
    if [ "$line_count" -gt 0 ]; then
        s1_only=$(echo "$s_lines" | head -1 | sed -E 's/.*contains ([0-9]+) constraints.*/\1/')
    fi

    # Extract S2 only constraints (second S*.json file mentioned, only if there are 2+ lines)
    if [ "$line_count" -gt 1 ]; then
        s2_only=$(echo "$s_lines" | tail -1 | sed -E 's/.*contains ([0-9]+) constraints.*/\1/')
    fi

    # Print results (use empty string if not found)
    echo "$filename, ${minor_changes:-}, ${major_changes:-}, ${s1_only:-}, ${s2_only:-}"
}

# Function to get sort key for filename
get_sort_key() {
    local filename="$1"

    # Extract the base pattern (e.g., "diff_s1_s2" from "diff_s1_s2_only")
    local base=$(echo "$filename" | sed -E 's/^(diff_s[0-9]+_s[0-9]+)(_only)?$/\1/')

    # Check if it has "_only" suffix
    local has_only=""
    if [[ "$filename" == *"_only" ]]; then
        has_only="1"
    else
        has_only="0"
    fi

    # Extract numbers for natural sorting
    local num1=$(echo "$base" | sed -E 's/diff_s([0-9]+)_s[0-9]+/\1/')
    local num2=$(echo "$base" | sed -E 's/diff_s[0-9]+_s([0-9]+)/\1/')

    # Create sort key: has_only|num1|num2
    # This puts non-"_only" files first (0), then "_only" files (1)
    # Within each group, sorts by first number, then second number
    printf "%s|%02d|%02d" "$has_only" "$num1" "$num2"
}

# Print header
echo "file_name, < 0.1, >0.1, s1_only, s2_only"

# Find all files without extensions, sort them, then process
temp_file=$(mktemp)
find . -type f -name "*" | while read -r file; do
    # Check if file has no extension (no dot in basename)
    basename_file=$(basename "$file")
    if [[ "$basename_file" != *.* ]]; then
        # Check if it's a text file
        if file "$file" | grep -q "text"; then
            sort_key=$(get_sort_key "$basename_file")
            echo "$sort_key|$file"
        fi
    fi
done | sort -t'|' -k1,1 -k2,2n -k3,3n | cut -d'|' -f4 > "$temp_file"

# Process the sorted files
while read -r file; do
    parse_file "$file"
done < "$temp_file"

# Clean up
rm "$temp_file"