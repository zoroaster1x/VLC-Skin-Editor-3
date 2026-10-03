#!/usr/bin/env bash
# Builds the GraalVM native image for the CLI, TUI and MCP entry points.
# The Swing window stays on the JVM; it is not verified inside a native image.
#
# Usage: tools/build-native.sh
# Requires GraalVM 25 (SDKMAN: sdk install java 25.4.4.1+1-graalce).
set -euo pipefail
cd "$(dirname "$0")/.."

GRAALVM_HOME="${GRAALVM_HOME:-$HOME/.sdkman/candidates/java/25.4.4.1+1-graalce}"
if [ ! -x "$GRAALVM_HOME/bin/native-image" ]; then
  echo "native-image not found under $GRAALVM_HOME" >&2
  echo "Install GraalVM 25: sdk install java 25.4.4.1+1-graalce" >&2
  exit 1
fi

./gradlew shadowJar -q
mkdir -p build/native

"$GRAALVM_HOME/bin/native-image" \
  --no-fallback \
  -H:+ReportExceptionStackTraces \
  -cp build/libs/vlc-skin-studio.jar \
  -o build/native/vlc-skin-studio \
  io.github.zoroaster1x.vlcskin.app.VlcSkinStudio

echo
echo "Built build/native/vlc-skin-studio"
echo "Keep the .so files that native-image placed next to it in the same directory."
echo "Check it with:"
echo "  build/native/vlc-skin-studio --version"
echo "  build/native/vlc-skin-studio render skin.xml -o preview.png"
echo "  build/native/vlc-skin-studio mcp   # stdio MCP server"
