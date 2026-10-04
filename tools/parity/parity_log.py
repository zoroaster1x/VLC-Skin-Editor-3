#!/usr/bin/env python3
"""Tiny step logger with timings for the parity tools.

Why this exists:
    A run that takes ninety seconds and prints one line at the end cannot be
    debugged. Every step here prints what it is doing and how long the previous
    step took, so a slow phase (VLC settling, image decode, capture) is obvious
    from the log alone and speed regressions are visible between runs.

Usage:
    from parity_log import StepLog
    log = StepLog("vlc-compare")
    log.step("rendering with MCP")
    ...
    log.step("capturing VLC")
    ...
    log.finish("compared 3 layouts")

Output goes to stderr so stdout stays machine readable (JSON reports).
Format: `[vlc-compare +12.3s (+4.5s)] message`
"""

import sys
import time


class StepLog:
    def __init__(self, name, stream=sys.stderr):
        self.name = name
        self.stream = stream
        self.start = time.monotonic()
        self.last = self.start

    def _emit(self, message):
        now = time.monotonic()
        total = now - self.start
        delta = now - self.last
        self.last = now
        print(f"[{self.name} +{total:.1f}s (+{delta:.1f}s)] {message}",
              file=self.stream, flush=True)

    def step(self, message):
        self._emit(message)

    def finish(self, message):
        self._emit(message)
