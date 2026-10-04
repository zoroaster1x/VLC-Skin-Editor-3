#!/usr/bin/env bash
# Builds the GraalVM native image for the CLI, TUI and MCP entry points.
# The Swing window stays on the JVM; it is not verified inside a native image.
#
# Usage: tools/build-native.sh
# Requires GraalVM 25 (for example an SDKMAN install, or set GRAALVM_HOME).
set -euo pipefail
cd "$(dirname "$0")/.."

GRAALVM_HOME="${GRAALVM_HOME:-}"
if [ -z "$GRAALVM_HOME" ] && command -v native-image >/dev/null 2>&1; then
  GRAALVM_HOME="$(cd "$(dirname "$(command -v native-image)")/.." && pwd)"
fi
if [ -z "$GRAALVM_HOME" ] || [ ! -x "$GRAALVM_HOME/bin/native-image" ]; then
  echo "native-image not found." >&2
  echo "Set GRAALVM_HOME to a GraalVM 25 install, for example:" >&2
  echo "  export GRAALVM_HOME=\"/path/to/graalvm-25\"" >&2
  echo "An SDKMAN install works too; make sure its bin/native-image exists." >&2
  exit 1
fi

./gradlew shadowJar -q
mkdir -p build/native

REFLECT_CONFIG="$PWD/src/main/resources/META-INF/native-image/dev.zoroaster1x/vlc-skin-studio/reflect-config.json"
JNI_CONFIG="$PWD/src/main/resources/META-INF/native-image/dev.zoroaster1x/vlc-skin-studio/jni-config.json"

"$GRAALVM_HOME/bin/native-image" \
  --no-fallback \
  -Djava.awt.headless=false \
  --initialize-at-run-time=sun.font.FontUtilities \
  -H:+ReportExceptionStackTraces \
  -H:ReflectionConfigurationFiles="$REFLECT_CONFIG" \
  -H:JNIConfigurationFiles="$JNI_CONFIG" \
  -H:IncludeResources='dev/zoroaster1x/vlcskin/(app/docs/.*|version.properties)' \
  -cp build/libs/vlc-skin-studio.jar \
  -o build/native/vlc-skin-studio \
  dev.zoroaster1x.vlcskin.app.VlcSkinStudio

python3 tools/generate-fontconfig.py build/native/fontconfig.properties || true

echo
echo "Built build/native/vlc-skin-studio"
echo "Keep the .so files that native-image placed next to it in the same directory."
echo "Check it with:"
echo "  build/native/vlc-skin-studio --version"
echo "  build/native/vlc-skin-studio render skin.xml -o preview.png"
echo "  build/native/vlc-skin-studio mcp   # stdio MCP server"
