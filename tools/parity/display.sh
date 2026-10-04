#!/usr/bin/env bash
# Manage an isolated X display for parity testing, without touching the
# operator's desktop session.
#
# Two providers:
#   native     Xvfb (and openbox when present) run on this machine.
#   container  a rootless podman container runs Xvfb, xdotool, openbox and
#              ImageMagick; the X socket is shared through /tmp/.X11-unix.
#
# Usage:
#   tools/parity/display.sh start [--provider auto|native|container]
#                                 [--display :99] [--size 1920x1080x24]
#                                 [--container parity-xvfb]
#   tools/parity/display.sh status
#   tools/parity/display.sh stop
#   tools/parity/display.sh window-list
#   tools/parity/display.sh screenshot [WINDOW] OUT
#   tools/parity/display.sh click X Y [BUTTON]
#   tools/parity/display.sh key KEY...
#   tools/parity/display.sh type TEXT
#   tools/parity/display.sh with -- COMMAND...
#
# After `start`, the state file under ${PARITY_STATE_DIR:-/tmp/vlc-skin-parity}
# records DISPLAY plus the command prefixes for xdotool, wmctrl and import, so
# Python helpers and shells can pick them up:
#
#   . "$PARITY_STATE_DIR/display.env"
#   "$PARITY_XDOTOOL" search --name "VLC Skin Studio"
#
# The provider is auto-detected: native when Xvfb is on PATH, container when
# podman is available, otherwise start fails with an explanation.

set -u

STATE_DIR="${PARITY_STATE_DIR:-/tmp/vlc-skin-parity}"
STATE_FILE="$STATE_DIR/display.env"
CONTAINER="${PARITY_CONTAINER:-parity-xvfb}"
DISPLAY_NAME="${PARITY_DISPLAY:-:99}"
SCREEN="${PARITY_SCREEN:-1920x1080x24}"
PROVIDER="${PARITY_PROVIDER:-auto}"
WIDTH="${SCREEN%x*}"; WIDTH="${WIDTH%x*}"
HEIGHT="${SCREEN#*x}"; HEIGHT="${HEIGHT%x*}"

log() { printf '%s\n' "$*" >&2; }
die() { log "error: $*"; exit 1; }

parse_args() {
    while [ $# -gt 0 ]; do
        case "$1" in
            --provider) PROVIDER="$2"; shift 2 ;;
            --display) DISPLAY_NAME="$2"; shift 2 ;;
            --size) SCREEN="$2"; shift 2 ;;
            --container) CONTAINER="$2"; shift 2 ;;
            *) die "unknown option $1" ;;
        esac
    done
}

detect_provider() {
    if [ "$PROVIDER" != "auto" ]; then
        return
    fi
    if command -v Xvfb >/dev/null 2>&1; then
        PROVIDER="native"
    elif command -v podman >/dev/null 2>&1; then
        PROVIDER="container"
    else
        die "no Xvfb on PATH and no podman; install one of them or run an X server yourself"
    fi
}

wait_for_display() {
    local tries=50
    while [ "$tries" -gt 0 ]; do
        if [ -S "/tmp/.X11-unix/X${DISPLAY_NAME#:}" ]; then
            if command -v xdpyinfo >/dev/null 2>&1; then
                if DISPLAY="$DISPLAY_NAME" xdpyinfo >/dev/null 2>&1; then
                    return 0
                fi
            else
                return 0
            fi
        fi
        tries=$((tries - 1))
        sleep 0.2
    done
    die "display $DISPLAY_NAME did not come up"
}

container_cmd() {
    podman exec -e DISPLAY="$DISPLAY_NAME" "$CONTAINER" "$@"
}

start_container() {
    if ! podman container exists "$CONTAINER" 2>/dev/null; then
        log "creating container $CONTAINER"
        podman run -d --name "$CONTAINER" --security-opt label=disable \
            -v /tmp/.X11-unix:/tmp/.X11-unix:rw sleep infinity >/dev/null
    fi
    if ! podman exec "$CONTAINER" command -v Xvfb >/dev/null 2>&1; then
        log "installing Xvfb, xdotool, openbox, wmctrl and ImageMagick in $CONTAINER"
        podman exec "$CONTAINER" dnf install -y --setopt=install_weak_deps=False \
            Xvfb xdotool openbox wmctrl ImageMagick >/dev/null 2>&1 \
            || die "package installation failed; install the tools manually"
    fi
    podman exec -e DISPLAY="$DISPLAY_NAME" -d "$CONTAINER" sh -c \
        'Xvfb "$0" -screen 0 "$1" -ac -nolisten tcp >/tmp/xvfb.log 2>&1 & sleep 1; command -v openbox >/dev/null && openbox --sm-disable >/tmp/openbox.log 2>&1 & wait' \
        "$DISPLAY_NAME" "$SCREEN" >/dev/null
}

start_native() {
    Xvfb "$DISPLAY_NAME" -screen 0 "$SCREEN" -ac -nolisten tcp >"$STATE_DIR/xvfb.log" 2>&1 &
    echo $! >"$STATE_DIR/xvfb.pid"
    if command -v openbox >/dev/null 2>&1; then
        DISPLAY="$DISPLAY_NAME" openbox --sm-disable >"$STATE_DIR/openbox.log" 2>&1 &
        echo $! >"$STATE_DIR/openbox.pid"
    fi
}

write_state() {
    local xdotool wmctrl import_cmd
    if command -v xdotool >/dev/null 2>&1; then
        xdotool="xdotool"
    else
        xdotool="podman exec -e DISPLAY=$DISPLAY_NAME $CONTAINER xdotool"
    fi
    if command -v wmctrl >/dev/null 2>&1; then
        wmctrl="wmctrl"
    else
        wmctrl="podman exec -e DISPLAY=$DISPLAY_NAME $CONTAINER wmctrl"
    fi
    if command -v import >/dev/null 2>&1; then
        import_cmd="import"
    elif command -v magick >/dev/null 2>&1; then
        import_cmd="magick import"
    else
        import_cmd="podman exec -e DISPLAY=$DISPLAY_NAME $CONTAINER import"
    fi
    mkdir -p "$STATE_DIR"
    {
        printf 'DISPLAY=%s\n' "$DISPLAY_NAME"
        printf 'PARITY_PROVIDER=%s\n' "$PROVIDER"
        printf 'PARITY_CONTAINER=%s\n' "$CONTAINER"
        printf 'PARITY_XDOTOOL=%s\n' "$xdotool"
        printf 'PARITY_WMCTRL=%s\n' "$wmctrl"
        printf 'PARITY_IMPORT=%s\n' "$import_cmd"
    } >"$STATE_FILE"
}

cmd_start() {
    mkdir -p "$STATE_DIR"
    detect_provider
    if [ -S "/tmp/.X11-unix/X${DISPLAY_NAME#:}" ]; then
        log "display $DISPLAY_NAME already exists; reusing it"
    elif [ "$PROVIDER" = "native" ]; then
        start_native
    elif [ "$PROVIDER" = "container" ]; then
        start_container
    else
        die "unknown provider $PROVIDER"
    fi
    wait_for_display
    write_state
    log "display $DISPLAY_NAME up via $PROVIDER (state: $STATE_FILE)"
}

cmd_stop() {
    if [ -f "$STATE_FILE" ]; then
        # shellcheck disable=SC1090
        . "$STATE_FILE"
        if [ "${PARITY_PROVIDER:-}" = "container" ]; then
            podman stop "$PARITY_CONTAINER" >/dev/null 2>&1 || true
        fi
    fi
    [ -f "$STATE_DIR/xvfb.pid" ] && kill "$(cat "$STATE_DIR/xvfb.pid")" 2>/dev/null || true
    [ -f "$STATE_DIR/openbox.pid" ] && kill "$(cat "$STATE_DIR/openbox.pid")" 2>/dev/null || true
    rm -f "$STATE_FILE"
    log "display stopped"
}

load_state() {
    [ -f "$STATE_FILE" ] || die "no display state at $STATE_FILE; run display.sh start first"
    # shellcheck disable=SC1090
    . "$STATE_FILE"
    export DISPLAY
}

cmd_status() {
    if [ -f "$STATE_FILE" ]; then
        cat "$STATE_FILE"
    else
        echo "no display started"
    fi
}

cmd_window_list() {
    load_state
    $PARITY_WMCTRL -l
}

cmd_screenshot() {
    load_state
    local window="${1:-root}" out="${2:?usage: screenshot [WINDOW] OUT}"
    mkdir -p "$(dirname "$out")"
    if [ "$window" = "root" ]; then
        $PARITY_IMPORT -window root "$out"
    else
        $PARITY_IMPORT -window "$window" "$out"
    fi
}

cmd_click() {
    load_state
    local x="$1" y="$2" button="${3:-1}"
    $PARITY_XDOTOOL mousemove "$x" "$y" click "$button"
}

cmd_key() {
    load_state
    $PARITY_XDOTOOL key "$@"
}

cmd_type() {
    load_state
    $PARITY_XDOTOOL type --delay 15 "$1"
}

cmd_with() {
    load_state
    [ "${1:-}" = "--" ] && shift
    [ $# -gt 0 ] || die "usage: with -- COMMAND..."
    "$@"
}

case "${1:-}" in
    start) shift; parse_args "$@"; cmd_start ;;
    stop) cmd_stop ;;
    status) cmd_status ;;
    window-list) cmd_window_list ;;
    screenshot) shift; cmd_screenshot "$@" ;;
    click) shift; cmd_click "$@" ;;
    key) shift; cmd_key "$@" ;;
    type) shift; cmd_type "$@" ;;
    with) shift; cmd_with "$@" ;;
    *) die "usage: display.sh {start|stop|status|window-list|screenshot|click|key|type|with} ..." ;;
esac
