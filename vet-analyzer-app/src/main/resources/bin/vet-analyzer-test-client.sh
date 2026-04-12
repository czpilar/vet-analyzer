#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$(dirname "$SCRIPT_DIR")"

echo "Starting Vet Analyzer Test Client v${project.version}..."
java -jar "$BASE_DIR/lib/vet-analyzer-test-client-${project.version}.jar"
