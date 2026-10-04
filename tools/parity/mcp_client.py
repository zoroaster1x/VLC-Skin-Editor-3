#!/usr/bin/env python3
"""Stdio MCP client for VLC Skin Studio, used by the parity harness.

The client speaks the same JSON-RPC dialect OpenCode and Claude use, so it can
drive a fresh `java -jar vlc-skin-studio.jar mcp` process without touching the
desktop session. Build it as a library:

    from mcp_client import McpClient
    with McpClient(jar) as client:
        client.call("open_skin", {"path": "theme.xml"})
        png = client.call_png("render_layout", {"window": "player", "layout": "main"})

or from the command line:

    mcp_client.py --jar build/libs/vlc-skin-studio.jar call get_xml '{}'
    mcp_client.py --jar build/libs/vlc-skin-studio.jar --open theme.xml call document_info
    mcp_client.py --jar build/libs/vlc-skin-studio.jar serve < steps.json

`serve` reads one JSON object per line, either a tool call
`{"tool": "add_item", "args": {...}}` or a control line `{"_": "open", "file": "..."}`,
and prints one response per line. Image blocks are written to --png-dir and
replaced by their path so the stream stays readable.
"""

import argparse
import base64
import json
import os
import subprocess
import sys


class McpError(RuntimeError):
    """Raised when the server reports an error for a request."""


class McpClient:
    """One MCP server process over stdio."""

    def __init__(self, jar, java=None, png_dir=None, env=None):
        self.jar = str(jar)
        self.java = java or os.environ.get("JAVA", "java")
        self.png_dir = png_dir
        self.image_index = 0
        self.process = subprocess.Popen(
            [self.java, "-jar", self.jar, "mcp"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
            env=env,
        )
        self.next_id = 1
        self.initialize()

    def initialize(self):
        result = self.request("initialize", {
            "protocolVersion": "2025-11-25",
            "capabilities": {},
            "clientInfo": {"name": "parity-harness", "version": "1.0"},
        })
        self.notify("notifications/initialized")
        return result

    def notify(self, method, params=None):
        message = {"jsonrpc": "2.0", "method": method}
        if params is not None:
            message["params"] = params
        self.process.stdin.write(json.dumps(message) + "\n")
        self.process.stdin.flush()

    def request(self, method, params):
        request_id = self.next_id
        self.next_id += 1
        self.process.stdin.write(json.dumps({
            "jsonrpc": "2.0", "id": request_id, "method": method, "params": params,
        }) + "\n")
        self.process.stdin.flush()
        while True:
            line = self.process.stdout.readline()
            if not line:
                error = self.process.stderr.read()
                raise McpError(f"the MCP server closed the connection\n{error}")
            try:
                message = json.loads(line)
            except json.JSONDecodeError:
                continue
            if message.get("id") == request_id:
                if "error" in message:
                    raise McpError(f"{method} failed: {message['error']}")
                return message.get("result", {})

    def call(self, name, arguments=None):
        """Call a tool and return the raw result content blocks."""
        result = self.request("tools/call", {"name": name, "arguments": arguments or {}})
        if result.get("isError"):
            text = "\n".join(part.get("text", "") for part in result.get("content", []))
            raise McpError(f"{name} failed: {text}")
        return result

    def call_text(self, name, arguments=None):
        """Call a tool and join its text blocks."""
        result = self.call(name, arguments)
        return "\n".join(part.get("text", "") for part in result.get("content", []))

    def call_json(self, name, arguments=None):
        """Call a tool whose text block is JSON and parse it."""
        text = self.call_text(name, arguments)
        try:
            return json.loads(text)
        except json.JSONDecodeError as exc:
            raise McpError(f"{name} did not return JSON: {text[:200]}") from exc

    def call_png(self, name, arguments=None, path=None):
        """Call a tool and return the first image block as PNG bytes."""
        result = self.call(name, arguments)
        for part in result.get("content", []):
            if part.get("type") == "image":
                data = base64.b64decode(part["data"])
                if path:
                    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
                    with open(path, "wb") as handle:
                        handle.write(data)
                return data
        return None

    def tools(self):
        """The tool catalog as returned by tools/list."""
        return self.request("tools/list", {}).get("tools", [])

    def open(self, skin):
        """Open a theme file and return the outcome text."""
        return self.call_text("open_skin", {"path": str(skin)})

    def save(self, path=None):
        """Save the document, optionally to a new path."""
        arguments = {"path": str(path)} if path else {}
        return self.call_text("save_skin", arguments)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        self.close()
        return False

    def close(self):
        try:
            self.process.terminate()
            self.process.wait(timeout=10)
        except Exception:
            self.process.kill()


def simplify(result, png_dir, image_index):
    """Turn content blocks into a JSON friendly record, dumping images."""
    out = {"isError": result.get("isError", False)}
    texts = []
    for part in result.get("content", []):
        if part.get("type") == "text":
            texts.append(part.get("text", ""))
        elif part.get("type") == "image":
            data = base64.b64decode(part["data"])
            record = {"bytes": len(data)}
            if png_dir:
                os.makedirs(png_dir, exist_ok=True)
                path = os.path.join(png_dir, f"image-{image_index}.png")
                with open(path, "wb") as handle:
                    handle.write(data)
                record["path"] = path
            out.setdefault("images", []).append(record)
    if texts:
        joined = "\n".join(texts)
        try:
            out["text"] = json.loads(joined)
        except json.JSONDecodeError:
            out["text"] = joined
    return out


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--jar", required=True, help="the fat jar to run")
    parser.add_argument("--java", help="java binary, default $JAVA or java")
    parser.add_argument("--open", dest="open_file", help="open this skin before the command")
    parser.add_argument("--png-dir", help="write image blocks here")
    parser.add_argument("command", nargs="?", default="call", choices=["call", "list", "serve"])
    parser.add_argument("tool", nargs="?", help="tool name for call, or filter for list")
    parser.add_argument("json", nargs="?", default="{}", help="tool arguments for call")
    args = parser.parse_args()

    client = McpClient(args.jar, java=args.java, png_dir=args.png_dir)
    try:
        if args.open_file:
            print(json.dumps({"tool": "open_skin",
                              "text": client.open(args.open_file)}), flush=True)
        if args.command == "call":
            if not args.tool:
                raise SystemExit("call needs a tool name")
            result = simplify(client.call(args.tool, json.loads(args.json)),
                              args.png_dir, client.image_index)
            print(json.dumps({"tool": args.tool, **result}))
        elif args.command == "list":
            tools = client.tools()
            if args.tool:
                tools = [tool for tool in tools if tool["name"] == args.tool]
            print(json.dumps(tools, indent=2))
        elif args.command == "serve":
            for line in sys.stdin:
                line = line.strip()
                if not line:
                    continue
                request = json.loads(line)
                if "_" in request:
                    if request["_"] == "open":
                        print(json.dumps({"tool": "open_skin",
                                          "text": client.open(request["file"])}), flush=True)
                    continue
                client.image_index += 1
                try:
                    result = simplify(client.call(request["tool"], request.get("args", {})),
                                      args.png_dir, client.image_index)
                    print(json.dumps({"tool": request["tool"], **result}), flush=True)
                except McpError as exc:
                    print(json.dumps({"tool": request["tool"], "error": str(exc)}), flush=True)
    finally:
        client.close()


if __name__ == "__main__":
    main()
