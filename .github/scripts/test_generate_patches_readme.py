"""Regression tests for the release README generator."""

import json
import subprocess
import tempfile
import unittest
from pathlib import Path


GENERATOR = Path(__file__).with_name("generate_patches_readme.py")


class ReadmeGeneratorTest(unittest.TestCase):
    def render(self, patches, expanded=False):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            catalog = root / "catalog.json"
            readme = root / "README.md"
            catalog.write_text(json.dumps({"version": "1.0", "patches": patches}), encoding="utf-8")
            marker = "PATCHES_START EXPANDED" if expanded else "PATCHES_START"
            readme.write_text(f"Before\n<!-- {marker} -->\nold\n<!-- PATCHES_END -->\nAfter\n", encoding="utf-8")
            subprocess.run(["python3", str(GENERATOR), "owner/repo", "dev", str(catalog), str(readme)],
                           check=True, capture_output=True)
            return readme.read_text(encoding="utf-8")

    def test_template_table_and_app_versions(self):
        result = self.render([{
            "name": "Example patch",
            "description": "First line\nSecond line",
            "options": [{"title": "App name"}],
            "compatiblePackages": [{
                "packageName": "example.app",
                "name": "Example",
                "apkFileType": "APKM",
                "targets": [
                    {"version": "1.0", "description": "Validated build"},
                    {"version": "2.0", "isExperimental": True},
                ],
            }],
        }])
        self.assertIn("**Package name:** `example.app`", result)
        self.assertIn("| 1.0 | 🧪&nbsp;2.0 |", result)
        self.assertIn("| Validated build |  |", result)
        self.assertIn("| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |", result)
        self.assertIn("| [Example patch](#example-patch) | First line<br>Second line | • App name |", result)
        self.assertNotIn("Versions / file type", result)
        self.assertNotIn("APKM", result)
        self.assertTrue(result.startswith("Before\n"))
        self.assertTrue(result.endswith("After\n"))
        self.assertNotIn("\nold\n", result)

    def test_small_catalog_stays_collapsed(self):
        result = self.render([
            {"name": "Universal"},
            {"name": "App patch", "compatiblePackages": [{"packageName": "example.app", "name": "Example"}]},
        ])
        self.assertEqual(result.count("<details>"), 2)
        self.assertEqual(result.count("</details>"), 2)
        self.assertNotIn("<details open>", result)
        self.assertIn("<summary>📦 Example&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>", result)
        self.assertIn("<summary>🌐 Universal&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>", result)

    def test_expanded_marker_keeps_sections_open(self):
        result = self.render([
            {"name": "Universal"},
            {"name": "App patch", "compatiblePackages": [{"packageName": "example.app", "name": "Example"}]},
        ], expanded=True)
        self.assertEqual(result.count("<details open>"), 2)
        self.assertEqual(result.count("</details>"), 2)
        self.assertNotIn("<details>", result)
        self.assertIn("<!-- PATCHES_START EXPANDED -->", result)


if __name__ == "__main__":
    unittest.main()
