#!/bin/sh
# VLC Skin Studio launcher for Linux and macOS.
#
# Runs the jar next to this script and passes every argument through. When Java
# is missing, or older than 25, it prints the Azul Zulu JRE download for the
# detected operating system and CPU instead of an UnsupportedClassVersionError.
# The download link matches the AZUL constant in the Java 8 entry point,
# src/launcher/java/dev/zoroaster1x/vlcskin/launch/Launcher.java.
set -u

DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1
JAR="$DIR/vlc-skin-studio.jar"
cd "$DIR" || exit 1

KERNEL=$(uname -s 2>/dev/null || printf 'unknown')
MACHINE=$(uname -m 2>/dev/null || printf 'unknown')
case "$KERNEL" in
    Darwin)
        OS=macos
        OS_NAME="macOS"
        ;;
    *)
        OS=linux
        OS_NAME="Linux"
        ;;
esac
case "$MACHINE" in
    arm64|aarch64)
        ARCH=arm-64-bit
        ARCH_NAME="ARM 64-bit"
        ;;
    *)
        ARCH=x86-64-bit
        ARCH_NAME="x86 64-bit"
        ;;
esac
AZUL="https://www.azul.com/downloads/?version=java-25-lts&package=jre&os=$OS&architecture=$ARCH#zulu"

missing_java() {
    printf '%s\n' \
        "JAVA NOT INSTALLED. Please download from:" \
        "  $AZUL" \
        "" \
        "Java 25 (Azul Zulu JRE) for $OS_NAME $ARCH_NAME: open the link, choose" \
        "the JRE build for your CPU, install or unpack it, then run this" \
        "script again." \
        "" \
        "Or install it with SDKMAN:" \
        "  curl -s \"https://get.sdkman.io\" | bash" \
        "  sdk install java 25-zulu"
}

old_java() {
    printf '%s\n' \
        "JAVA 25 OR NEWER IS REQUIRED. You are running Java $VERSION." \
        "Please download from:" \
        "  $AZUL" \
        "" \
        "Open the link, choose Java 25 and the JRE build for $OS_NAME" \
        "($ARCH_NAME), install or unpack it, then run this script again."
}

JAVA=""
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA=java
fi
if [ -z "$JAVA" ]; then
    missing_java
    exit 1
fi

VERSION=$("$JAVA" -version 2>&1 | sed -n '1s/.*version "\([^"]*\)".*/\1/p')
[ -n "$VERSION" ] || VERSION=unknown
MAJOR=${VERSION%%.*}
if [ "$MAJOR" = "1" ]; then
    MAJOR=${VERSION#1.}
    MAJOR=${MAJOR%%.*}
fi
case "$MAJOR" in
    ''|*[!0-9]*)
        # An unknown runtime is left to the jar, its entry point can explain.
        ;;
    *)
        if [ "$MAJOR" -lt 25 ]; then
            old_java
            exit 1
        fi
        ;;
esac

if [ ! -f "$JAR" ]; then
    printf '%s\n' \
        "vlc-skin-studio.jar is missing next to this script." \
        "Unzip the whole release package and run run.sh from that folder." >&2
    exit 1
fi

exec "$JAVA" -jar "$JAR" "$@"
