import importlib.util
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "manifest_security_diff.py"
SPEC = importlib.util.spec_from_file_location("manifest_security_diff", SCRIPT)
DIFF = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(DIFF)

MANIFEST = """
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.INTERNET" />
  <application>
    <provider android:name=".Provider" android:authorities="com.example.files"
      android:exported="false" android:grantUriPermissions="true" />
  </application>
</manifest>
"""


class ManifestSecurityDiffTest(unittest.TestCase):
    def test_identical_manifests_have_no_delta(self):
        delta = DIFF.security_delta(MANIFEST, MANIFEST)
        self.assertTrue(
            all(not value["added"] and not value["removed"] for value in delta.values())
        )

    def test_exported_component_change_is_reported(self):
        patched = MANIFEST.replace(
            'android:exported="false"', 'android:exported="true"'
        )
        delta = DIFF.security_delta(MANIFEST, patched)
        self.assertIn("false", str(delta["components"]["removed"]))
        self.assertIn("true", str(delta["components"]["added"]))

    def test_permission_addition_is_reported(self):
        patched = MANIFEST.replace(
            "<application>",
            '<uses-permission android:name="android.permission.READ_CONTACTS" />\n  <application>',
        )
        delta = DIFF.security_delta(MANIFEST, patched)
        self.assertEqual(
            delta["permissions"]["added"],
            ["android.permission.INTERNET", "android.permission.READ_CONTACTS"][-1:],
        )


if __name__ == "__main__":
    unittest.main()
