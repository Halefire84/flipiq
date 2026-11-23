#!/bin/sh
# Gradle wrapper script
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
exec gradle "$@" --project-dir "$SCRIPT_DIR"
