#!/usr/bin/env python3
"""Run a scripted edit session against VLC Skin Studio over MCP.

A scenario is a JSON file. Paths support {corpus}, {work} and {jar}
placeholders. Every tool result is appended to results.jsonl and images are
written next to it, so a run leaves a reviewable trail.

    {
      "name": "velocity-structural-edit",
      "open": "{corpus}/velocity/theme.xml",
      "steps": [
        {"tool": "add_layout",
         "args": {"window": "player", "id": "testlayout", "width": 300, "height": 200}},
        {"tool": "set_item_property",
         "args": {"id": "normal.topbar", "name": "width", "value": "700"}},
        {"tool": "render_layout",
         "args": {"window": "player", "layout": "main"}, "png": "main.png"},
        {"tool": "save_skin", "args": {"path": "{work}/out/theme.xml"}}
      ],
      "expect": [
        {"tool": "document_info", "args": {}, "check": {"layouts": 5}}
      ]
    }

`check` values are compared against the parsed text block of the tool result;
a nested dict compares only the given keys, lists compare exactly.

Usage:
    tools/parity/scenario.py --jar build/libs/vlc-skin-studio.jar \
        --scenario tools/parity/scenarios/velocity-edit.json \
        --corpus build/parity/corpus --work build/parity/runs
"""

import argparse
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mcp_client import McpClient, McpError  # noqa: E402


def substitute(value, variables):
    if isinstance(value, str):
        for key, replacement in variables.items():
            value = value.replace("{" + key + "}", replacement)
        return value
    if isinstance(value, dict):
        return {key: substitute(item, variables) for key, item in value.items()}
    if isinstance(value, list):
        return [substitute(item, variables) for item in value]
    return value


def matches(expected, actual, path=""):
    """Nested subset comparison for expectation checks."""
    problems = []
    if isinstance(expected, dict):
        if not isinstance(actual, dict):
            return [f"{path}: expected an object, got {type(actual).__name__}"]
        for key, value in expected.items():
            if key not in actual:
                problems.append(f"{path}.{key}: missing")
            else:
                problems.extend(matches(value, actual[key], f"{path}.{key}"))
    elif expected != actual:
        problems.append(f"{path or 'value'}: expected {expected!r}, got {actual!r}")
    return problems


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jar", required=True)
    parser.add_argument("--scenario", required=True)
    parser.add_argument("--corpus", default="build/parity/corpus")
    parser.add_argument("--work", default="build/parity/runs")
    parser.add_argument("--java", help="java binary, default $JAVA or java")
    args = parser.parse_args()

    with open(args.scenario, encoding="utf-8") as handle:
        scenario = json.load(handle)

    name = scenario.get("name") or os.path.splitext(os.path.basename(args.scenario))[0]
    out_dir = os.path.join(args.work, name)
    os.makedirs(out_dir, exist_ok=True)
    variables = {
        "corpus": os.path.abspath(args.corpus),
        "work": os.path.abspath(out_dir),
        "jar": os.path.abspath(args.jar),
        "cwd": os.getcwd(),
    }
    log_path = os.path.join(out_dir, "results.jsonl")
    failures = []

    with McpClient(args.jar, java=args.java, png_dir=out_dir) as client:
        opened = substitute(scenario.get("open"), variables)
        if opened:
            text = client.open(opened)
            record = {"tool": "open_skin", "text": text}
            print(json.dumps(record))
            with open(log_path, "a", encoding="utf-8") as log:
                log.write(json.dumps(record) + "\n")

        for index, step in enumerate(scenario.get("steps", [])):
            tool = step["tool"]
            arguments = substitute(step.get("args", {}), variables)
            try:
                client.image_index += 1
                result = client.call(tool, arguments)
            except McpError as exc:
                failures.append(f"step {index} {tool}: {exc}")
                print(f"FAIL step {index} {tool}: {exc}", file=sys.stderr)
                continue
            record = {"step": index, "tool": tool, "args": arguments,
                      "isError": result.get("isError", False)}
            if step.get("png"):
                data = None
                for part in result.get("content", []):
                    if part.get("type") == "image":
                        import base64
                        data = base64.b64decode(part["data"])
                        break
                if data:
                    png_path = os.path.join(out_dir, substitute(step["png"], variables))
                    os.makedirs(os.path.dirname(os.path.abspath(png_path)), exist_ok=True)
                    with open(png_path, "wb") as handle:
                        handle.write(data)
                    record["png"] = png_path
                else:
                    failures.append(f"step {index} {tool}: no image in the result")
            with open(log_path, "a", encoding="utf-8") as log:
                log.write(json.dumps(record) + "\n")
            print(json.dumps(record))

        for index, check in enumerate(scenario.get("expect", [])):
            tool = check["tool"]
            arguments = substitute(check.get("args", {}), variables)
            try:
                actual = client.call_json(tool, arguments)
            except McpError as exc:
                failures.append(f"check {index} {tool}: {exc}")
                continue
            problems = matches(check.get("check", {}), actual)
            record = {"check": index, "tool": tool, "problems": problems, "actual": actual}
            with open(log_path, "a", encoding="utf-8") as log:
                log.write(json.dumps(record) + "\n")
            if problems:
                failures.extend(f"check {index} {tool} {problem}" for problem in problems)
                print(json.dumps(record))
            else:
                print(f"check {index} {tool}: ok")

    if failures:
        print(f"\n{len(failures)} failure(s):", file=sys.stderr)
        for failure in failures:
            print(f"  {failure}", file=sys.stderr)
        return 1
    print(f"scenario {name}: ok, artifacts in {out_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
