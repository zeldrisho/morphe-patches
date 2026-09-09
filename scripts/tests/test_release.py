#!/usr/bin/env python3
"""Release fixtures: only temporary repositories and files are modified.

Run: python3 -m unittest discover -s scripts/tests -p 'test_release.py' -v
"""

import os
import re
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = (ROOT / "scripts/prepare-release.sh").read_text(encoding="utf-8")
PREFLIGHT, PROMOTE, _ = re.findall(r"<<'PY'\n(.*?)\nPY", SCRIPT, re.DOTALL)
EXTRACTOR = ROOT / ".github/scripts/extract_release_notes.py"
DATE = "2026-09-09"
REPO = "example/patches"
BULLET = "* **Threads:** A change.\n"


def heading(version, linked=False):
    label = (
        f"[{version}](https://github.com/{REPO}/compare/v1.0.0...v{version})"
        if linked
        else version
    )
    return f"## {label} ({DATE})"


def entry(version, linked=False):
    return heading(version, linked) + "\n\n### ✨ New Features\n" + BULLET


class ReleaseFixtures(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.cwd = Path(self.temp.name)
        # Ignore host Git configuration/hooks; all commits/tags are fixtures.
        self.env = dict(os.environ, GIT_CONFIG_GLOBAL=os.devnull,
                        GIT_CONFIG_NOSYSTEM="1")
        self.git("init", "-b", "main")
        self.git("config", "user.name", "Release Fixture")
        self.git("config", "user.email", "fixture@example.invalid")
        self.git("config", "core.hooksPath", os.devnull)
        self.git("commit", "--allow-empty", "-m", "fixture")
        self.changelog = self.cwd / "CHANGELOG.md"

    def git(self, *args):
        return subprocess.run(
            ["git", *args], cwd=self.cwd, env=self.env, check=True,
            text=True, capture_output=True,
        ).stdout.strip()

    def write_changelog(self, rest=""):
        self.changelog.write_text(
            "# Changelog\n\n## Unreleased\n\n### 🚀 Updated App Support\n"
            + BULLET + "\n" + rest, encoding="utf-8",
        )

    def preflight(self, version="1.1.0", success=True):
        before = self.changelog.read_bytes()
        result = subprocess.run(
            [sys.executable, "-c", PREFLIGHT, version, REPO],
            cwd=self.cwd, env=self.env, text=True, capture_output=True,
        )
        self.assertEqual(result.returncode == 0, success, result.stderr)
        self.assertEqual(self.changelog.read_bytes(), before)
        return result

    def promote(self, version, prev):
        subprocess.run(
            [sys.executable, "-c", PROMOTE, version, DATE, REPO, prev],
            cwd=self.cwd, env=self.env, check=True, capture_output=True,
        )
        return self.changelog.read_text(encoding="utf-8")

    def test_existing_shell_safeguards(self):
        # Execute only the original shell guards, never the real staging steps.
        guards = SCRIPT.split('REPO="', 1)[0]

        def check(version, error=None):
            result = subprocess.run(
                ["bash", "-c", guards, "prepare-release.sh", version],
                cwd=self.cwd, env=self.env, text=True, capture_output=True,
            )
            if error:
                self.assertNotEqual(result.returncode, 0)
                self.assertIn(error, result.stderr)
            else:
                self.assertEqual(result.returncode, 0, result.stderr)

        # The script computes its root from BASH_SOURCE[0]. Point the fixture's
        # script path there while keeping execution limited to the guard prefix.
        guards = guards.replace(
            'PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"',
            'PROJECT_DIR="$PWD"',
        )
        check("1.1.0")
        check("1.1.0-dev.1", "Version must be X.Y.Z")
        self.git("checkout", "-b", "feature")
        check("1.1.0", "Run on main")
        self.git("checkout", "main")
        self.write_changelog()
        self.git("add", "CHANGELOG.md")
        check("1.1.0", "Working tree is dirty")
        self.git("commit", "-m", "fixture changelog")
        self.changelog.write_text(self.changelog.read_text() + "\n")
        check("1.1.0", "Working tree is dirty")
        self.git("checkout", "--", "CHANGELOG.md")
        self.git("tag", "v1.1.0")
        check("1.1.0", "Tag v1.1.0 already exists")

    def test_workflow_validation_precedes_build(self):
        workflow = (ROOT / ".github/workflows/release.yml").read_text()
        extractor = workflow.index("python3 .github/scripts/extract_release_notes.py")
        self.assertLess(extractor, workflow.index("- name: Set up Java"))
        self.assertLess(extractor, workflow.index("- name: Test and build bundle"))

    def test_first_release(self):
        self.write_changelog()
        self.assertEqual(self.preflight("1.0.0").stdout.strip(), "")
        text = self.promote("1.0.0", "")
        self.assertIn(heading("1.0.0"), text)
        self.assertNotIn("/compare/", text)

    def test_subsequent_release_preserves_old_sections(self):
        for linked in (False, True):
            with self.subTest(linked=linked):
                self.git("tag", "-f", "-a", "v1.0.0", "-m", "fixture release")
                self.git("tag", "-f", "v1.1.0")
                old = entry("1.1.0", linked) + "\n" + entry("1.0.0")
                self.write_changelog(old)
                prev = self.preflight("1.2.0").stdout.strip()
                self.assertEqual(prev, "1.1.0")
                text = self.promote("1.2.0", prev)
                self.assertTrue(text.endswith(old))
                self.assertIn(
                    "## [1.2.0](https://github.com/example/patches/compare/"
                    f"v1.1.0...v1.2.0) ({DATE})", text,
                )
                self.assertTrue(text.startswith("# Changelog\n\n## Unreleased\n\n"))

    def test_missing_tag(self):
        self.write_changelog(entry("1.0.0"))
        self.assertIn("Missing tag v1.0.0", self.preflight(success=False).stderr)

    def test_untagged_staging(self):
        self.git("tag", "v1.0.0")
        self.write_changelog(entry("1.1.0", True) + "\n" + entry("1.0.0"))
        self.assertIn("Missing tag v1.1.0", self.preflight("1.2.0", False).stderr)
        self.assertIn("already contains", self.preflight("1.1.0", False).stderr)

    def test_out_of_order_requested_version(self):
        self.git("tag", "v1.0.0")
        self.write_changelog(entry("1.0.0"))
        self.assertIn("must be greater", self.preflight("0.9.0", False).stderr)

    def test_out_of_order_history(self):
        self.git("tag", "v1.0.0")
        self.git("tag", "v1.1.0")
        self.write_changelog(entry("1.0.0") + "\n" + entry("1.1.0", True))
        self.assertIn("newest-first", self.preflight("1.2.0", False).stderr)

    def test_higher_reachable_tag(self):
        self.git("tag", "v1.0.0")
        self.git("tag", "v1.10.0")
        self.write_changelog(entry("1.0.0"))
        self.assertIn("highest reachable", self.preflight("1.11.0", False).stderr)

    def test_numeric_tag_order_and_prerelease_ignored(self):
        for tag in ("v1.9.0", "v1.10.0", "v9.0.0-dev.1", "unrelated"):
            self.git("tag", tag)
        self.write_changelog(entry("1.10.0", True) + "\n" + entry("1.9.0"))
        self.assertEqual(self.preflight("1.11.0").stdout.strip(), "1.10.0")

    def test_unreachable_previous_tag(self):
        self.git("checkout", "-b", "other")
        self.git("commit", "--allow-empty", "-m", "other")
        self.git("tag", "v1.0.0")
        self.git("checkout", "main")
        self.write_changelog(entry("1.0.0"))
        self.assertIn("not reachable", self.preflight(success=False).stderr)

    def test_tags_without_entries(self):
        self.git("tag", "v1.0.0")
        self.write_changelog()
        self.assertIn("no released entries", self.preflight(success=False).stderr)

    def test_shallow_history(self):
        self.write_changelog()
        (self.cwd / ".git/shallow").write_text(self.git("rev-parse", "HEAD") + "\n")
        self.assertIn("complete history", self.preflight(success=False).stderr)

    def test_empty_unreleased(self):
        self.write_changelog(entry("1.0.0"))
        self.changelog.write_text(self.changelog.read_text().replace(BULLET, "", 1))
        self.assertIn("no '*' bullets", self.preflight(success=False).stderr)

    def test_extraction_bare_and_linked_boundaries(self):
        for target_linked in (False, True):
            for next_linked in (False, True):
                with self.subTest(target=target_linked, boundary=next_linked):
                    self.write_changelog(
                        entry("1.2.0", True) + "\n" + entry("1.1.0", target_linked)
                        + "\n" + entry("1.0.0", next_linked)
                    )
                    output = self.cwd / "notes.md"
                    subprocess.run(
                        [sys.executable, str(EXTRACTOR), str(self.changelog),
                         "1.1.0", str(output)], check=True, capture_output=True,
                    )
                    self.assertEqual(output.read_text(), "### ✨ New Features\n" + BULLET)

    def test_malformed_and_reference_headings_rejected(self):
        for bad in (
            f"## [1.1.0] ({DATE})",
            f"## [1.1.0][compare] ({DATE})",
            f"## [1.1.0] - {DATE}",
            f"## [1.1.0 (https://example.com) ({DATE})",
            f"## 1.1.0](https://example.com) ({DATE})",
            "## 1.1.0",
            f"## 1x1x0 ({DATE})",
        ):
            with self.subTest(heading=bad):
                self.write_changelog(bad + "\n" + BULLET + "\n[compare]: https://example.com\n")
                self.assertIn("Unrecognized release heading", self.preflight("1.2.0", False).stderr)
                result = subprocess.run(
                    [sys.executable, str(EXTRACTOR), str(self.changelog),
                     "1.1.0", str(self.cwd / "notes.md")], capture_output=True,
                )
                self.assertNotEqual(result.returncode, 0)

    def test_empty_extracted_body_rejected(self):
        self.changelog.write_text(heading("1.1.0", True) + "\n\n" + entry("1.0.0"))
        result = subprocess.run(
            [sys.executable, str(EXTRACTOR), str(self.changelog),
             "1.1.0", str(self.cwd / "notes.md")], capture_output=True,
        )
        self.assertNotEqual(result.returncode, 0)


if __name__ == "__main__":
    unittest.main()
