#!/usr/bin/env python3
"""Semantic XML diff for VLC skins2 theme files.

Attribute order in the output is canonical, not byte identical, so round trip
checks compare the parsed documents: element order, tags, attributes and text.
Comments are ignored by default because the original editor rewrites its
"Created using" banner on every save.

Usage:
    tools/parity/compare_xml.py original.xml roundtrip.xml
    tools/parity/compare_xml.py --json build/parity/diff.json a.xml b.xml
    tools/parity/compare_xml.py --comments a.xml b.xml

Exit code 0 when the documents match, 1 when they differ, 2 on a parse error.
"""

import argparse
import json
import sys
import xml.etree.ElementTree as ET

COMMENT = ET.Comment


def parse(path, keep_comments):
    parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=keep_comments))
    try:
        return ET.parse(path, parser=parser).getroot()
    except ET.ParseError as exc:
        print(f"cannot parse {path}: {exc}", file=sys.stderr)
        sys.exit(2)


def label(element, index, same_tag_count):
    if same_tag_count > 1:
        return f"{element.tag}[{index}]"
    return str(element.tag)


def child_positions(children):
    """Index among siblings sharing the same tag, for readable paths."""
    counts = {}
    positions = []
    for child in children:
        key = child.tag if isinstance(child.tag, str) else "comment"
        counts[key] = counts.get(key, 0) + 1
        positions.append(counts[key])
    totals = {}
    for child in children:
        key = child.tag if isinstance(child.tag, str) else "comment"
        totals[key] = totals.get(key, 0) + 1
    return positions, totals


def compare(a, b, path, differences, ignore_attributes):
    if a.tag != b.tag:
        differences.append({"path": path, "kind": "tag",
                            "expected": a.tag, "actual": b.tag})
        return
    if a.tag is COMMENT:
        if (a.text or "").strip() != (b.text or "").strip():
            differences.append({"path": path, "kind": "comment",
                                "expected": a.text, "actual": b.text})
        return

    for name in sorted(set(a.attrib) | set(b.attrib)):
        if name in ignore_attributes:
            continue
        if name not in b.attrib:
            differences.append({"path": f"{path}/@{name}", "kind": "missing attribute",
                                "expected": a.attrib[name], "actual": None})
        elif name not in a.attrib:
            differences.append({"path": f"{path}/@{name}", "kind": "extra attribute",
                                "expected": None, "actual": b.attrib[name]})
        elif a.attrib[name] != b.attrib[name]:
            differences.append({"path": f"{path}/@{name}", "kind": "attribute value",
                                "expected": a.attrib[name], "actual": b.attrib[name]})

    a_children = list(a)
    b_children = list(b)
    positions, _ = child_positions(a_children)
    for index, child in enumerate(a_children):
        name = label(child, positions[index], len(a_children))
        if index >= len(b_children):
            differences.append({"path": f"{path}/{name}", "kind": "missing element",
                                "expected": child.tag if isinstance(child.tag, str) else "comment",
                                "actual": None})
        else:
            compare(child, b_children[index], f"{path}/{name}", differences, ignore_attributes)
    for index in range(len(a_children), len(b_children)):
        child = b_children[index]
        differences.append({"path": f"{path}/*[{index + 1}]", "kind": "extra element",
                            "expected": None,
                            "actual": child.tag if isinstance(child.tag, str) else "comment"})

    if a.tag is not COMMENT:
        a_text = (a.text or "").strip()
        b_text = (b.text or "").strip()
        if a_text != b_text:
            differences.append({"path": path, "kind": "text",
                                "expected": a_text, "actual": b_text})


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("expected")
    parser.add_argument("actual")
    parser.add_argument("--json", help="write the difference list here")
    parser.add_argument("--comments", action="store_true", help="also compare comments")
    parser.add_argument("--ignore-attribute", action="append", default=[],
                        help="attribute name to skip, repeatable")
    parser.add_argument("--max", type=int, default=100, help="limit printed differences")
    args = parser.parse_args()

    a = parse(args.expected, args.comments)
    b = parse(args.actual, args.comments)
    differences = []
    compare(a, b, a.tag, differences, set(args.ignore_attribute))

    if args.json:
        with open(args.json, "w", encoding="utf-8") as handle:
            json.dump(differences, handle, indent=2, ensure_ascii=False)
    for difference in differences[:args.max]:
        expected = difference["expected"]
        actual = difference["actual"]
        print(f"{difference['kind']:>18}  {difference['path']}")
        if expected is not None and actual is not None:
            print(f"{'':>18}    expected: {expected}")
            print(f"{'':>18}    actual:   {actual}")
    if len(differences) > args.max:
        print(f"... and {len(differences) - args.max} more")
    print(f"{len(differences)} difference(s)")
    return 0 if not differences else 1


if __name__ == "__main__":
    sys.exit(main())
