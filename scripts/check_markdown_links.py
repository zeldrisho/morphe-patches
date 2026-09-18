#!/usr/bin/env python3
"""Check repository-relative Markdown links without network access."""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LINK = re.compile(r"!?\[[^]]*\]\(([^)\s]+)(?:\s+[^)]*)?\)")
HEADING = re.compile(r"^#{1,6}\s+(.+?)\s*#*\s*$")


def slug(value):
    value = re.sub(r"[`*_~]", "", value).lower()
    value = re.sub(r"[^\w\s-]", "", value)
    return re.sub(r"\s+", "-", value).strip("-")


def anchors(path):
    return {
        slug(match.group(1))
        for line in path.read_text(encoding="utf-8").splitlines()
        if (match := HEADING.match(line))
    }


def main():
    errors = []
    for path in ROOT.rglob("*.md"):
        if any(part in {".git", "build", ".gradle", "analysis"} for part in path.parts):
            continue
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            for raw in LINK.findall(line):
                if raw.startswith(("http://", "https://", "mailto:", "#")):
                    target, fragment = (
                        (raw.split("#", 1) + [""])[:2]
                        if raw.startswith("#")
                        else (None, None)
                    )
                    if target is None:
                        continue
                    if fragment not in anchors(path):
                        errors.append(f"{path}:{number}: missing anchor #{fragment}")
                    continue
                target, _, fragment = raw.partition("#")
                destination = (path.parent / target).resolve()
                if not destination.is_file() or not str(destination).startswith(
                    str(ROOT.resolve())
                ):
                    errors.append(f"{path}:{number}: missing local link {raw}")
                elif fragment and fragment not in anchors(destination):
                    errors.append(f"{path}:{number}: missing anchor {raw}")
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
