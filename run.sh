#!/usr/bin/env bash
# Convenience launcher. Builds the fat jar once and runs it.
# Usage: ./run.sh [gui|mcp|tui|render|validate|inspect|new|vlt|examples] [args...]
set -euo pipefail
cd "$(dirname "$0")"

JAR="build/libs/vlc-skin-studio.jar"
if [ ! -f "$JAR" ] || [ "${REBUILD:-0}" = "1" ]; then
  ./gradlew shadowJar -q
fi

if [ "$#" -eq 0 ]; then
  exec java -jar "$JAR" gui
fi
exec java -jar "$JAR" "$@"
