#!/bin/bash

generate_images() {
    PUML_DIR="$1"
    OUTPUT_DIR="$2"

    mkdir -p "$OUTPUT_DIR"

    for puml_file in "$PUML_DIR"/*.puml; do
        base_name=$(basename "$puml_file" .puml)

        plantuml -tpng "$puml_file" -o "$OUTPUT_DIR"

        echo "Generated image for $puml_file"
    done
}

OUTPUT_DIR="images"

generate_images "uml/classes" "$OUTPUT_DIR"

generate_images "uml" "$OUTPUT_DIR"

echo "All images have been updated."
