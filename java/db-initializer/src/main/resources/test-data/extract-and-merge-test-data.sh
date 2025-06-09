#!/usr/bin/env bash

DEST_DIR="train-ticket-jaeger-all"
mkdir -p "$DEST_DIR"

counter=0
sample_index=0

archive_dest_dir() {
    formatted_index=$(printf "%02d" "$sample_index")
    archive_name="train-ticket-sample-${formatted_index}.tar.gz"
    echo "Archiving $DEST_DIR to $archive_name ..."
    tar -czf "$archive_name" "$DEST_DIR"

    # Cleanup after archiving
    echo "Cleaning up $DEST_DIR ..."
    rm -f "$DEST_DIR"/*.json

    ((sample_index++))
}


# Process each .tar.gz archive in the current directory
for archive in *.tar.gz; do
    [ -e "$archive" ] || continue

    base_name="${archive%.tar.gz}"
    echo "extracting $base_name ..."

    temp_dir="tmp_extract_$base_name"
    mkdir "$temp_dir"

    tar -xzf "$archive" -C "$temp_dir"

    traces_dir="$temp_dir/traces-jaeger"

    if [ -d "$traces_dir" ]; then
        for json_file in "$traces_dir"/*.json; do
            [ -e "$json_file" ] || continue
            filename=$(basename "$json_file")
            cp "$json_file" "$DEST_DIR/${base_name}__$filename"
        done
    else
        echo "Warning: $traces_dir not found in $archive"
    fi

    rm -rf "$temp_dir"

    ((counter++))

    # Archive every 10th extraction
    if (( counter % 10 == 0 )); then
        archive_dest_dir
    fi
done

if (( counter % 10 != 0 )); then
    archive_dest_dir
fi

echo "Done processing archives."

