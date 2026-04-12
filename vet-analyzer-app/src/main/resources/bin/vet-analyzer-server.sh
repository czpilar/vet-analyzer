#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"

echo "Starting Vet Analyzer Server v${project.version}..."
java -jar "$BASE_DIR/lib/vet-analyzer-server-${project.version}.jar" --spring.config.additional-location=file:$BASE_DIR/config/
