#!/usr/bin/env bash
# Convenience launcher. Builds the fat jar once, takes a private snapshot of it
# and runs that. The snapshot matters: Gradle rewrites the jar in place, and a
# running JVM that loads a class lazily from a truncated jar dies with
# ClassNotFoundException. The snapshot means builds can run while the app is up.
# Usage: ./run.sh [gui|mcp|tui|render|validate|inspect|new|vlt|examples] [args...]
set -euo pipefail
cd "$(dirname "$0")"

JAR="build/libs/vlc-skin-studio.jar"
if [ ! -f "$JAR" ] || [ "${REBUILD:-0}" = "1" ]; then
  ./gradlew shadowJar -q
fi

RUN_DIR="build/run"
RUN_JAR="$RUN_DIR/vlc-skin-studio.jar"
mkdir -p "$RUN_DIR"
cp -f "$JAR" "$RUN_JAR.tmp"
mv -f "$RUN_JAR.tmp" "$RUN_JAR"

if [ "$#" -eq 0 ]; then
  exec java -jar "$RUN_JAR" gui
fi
exec java -jar "$RUN_JAR" "$@"
