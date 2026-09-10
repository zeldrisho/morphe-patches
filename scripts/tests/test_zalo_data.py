#!/usr/bin/env python3
"""Offline regressions for backup/restore-zalo-data.sh via a fake adb.

Run: python3 -m unittest discover -s scripts/tests -p 'test_zalo_data.py' -v
No device needed: a fake `adb` on PATH emulates devices/pull/push/shell.
"""

import os
import stat
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPTS = ROOT / "scripts"

FAKE_ADB = r"""#!/usr/bin/env python3
import os, pathlib, sys

def devices():
    print("List of devices attached")
    for line in os.environ.get("FAKE_DEVICES", "").splitlines():
        if line.strip():
            print(line)

def main(args):
    if args == ["devices"]:
        devices()
    elif args[:2] == ["shell", "test"]:
        sys.exit(0 if os.environ.get("FAKE_HAS_SRC", "1") == "1" else 1)
    elif args[0] == "pull":
        dst = pathlib.Path(args[2]) / "com.zing.zalo"
        dst.mkdir(parents=True, exist_ok=True)
        for i in range(int(os.environ.get("FAKE_PULL_FILES", "3"))):
            (dst / f"photo{i}.jpg").write_text("x")
    elif args[0] == "push":
        pass
    elif args[:2] == ["shell", "ls"]:
        for _ in range(int(os.environ.get("FAKE_LS_LINES", "5"))):
            print("photo0.jpg")
    else:
        sys.exit(99)

main(sys.argv[1:])
"""


class ZaloDataScripts(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        bindir = self.root / "bin"
        bindir.mkdir()
        adb = bindir / "adb"
        adb.write_text(FAKE_ADB, encoding="utf-8")
        adb.chmod(adb.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)
        self.env = dict(
            os.environ, PATH=str(bindir) + os.pathsep + os.environ.get("PATH", "")
        )
        # Default: one device, source present.
        self.env.setdefault("FAKE_DEVICES", "emulator-5554\tdevice")

    def tearDown(self):
        self.tmp.cleanup()

    def run_script(self, script, *args, cwd=None):
        return subprocess.run(
            ["bash", str(SCRIPTS / script), *args],
            cwd=str(cwd or self.root),
            env=self.env,
            capture_output=True,
            text=True,
        )

    # backup-zalo-data.sh

    def test_backup_no_device(self):
        self.env["FAKE_DEVICES"] = ""
        proc = self.run_script("backup-zalo-data.sh")
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("exactly one", proc.stderr)
        self.assertEqual(list(self.root.glob("backup_zalo_*")), [])

    def test_backup_multiple_devices(self):
        self.env["FAKE_DEVICES"] = "a\tdevice\nb\tdevice"
        proc = self.run_script("backup-zalo-data.sh")
        self.assertNotEqual(proc.returncode, 0)
        self.assertEqual(list(self.root.glob("backup_zalo_*")), [])

    def test_backup_missing_source(self):
        self.env["FAKE_HAS_SRC"] = "0"
        proc = self.run_script("backup-zalo-data.sh")
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("missing", proc.stderr.lower())

    def test_backup_happy_path(self):
        proc = self.run_script("backup-zalo-data.sh")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        backups = list(self.root.glob("backup_zalo_*"))
        self.assertEqual(len(backups), 1)
        self.assertTrue((backups[0] / "com.zing.zalo").is_dir())
        self.assertIn("3 files", proc.stdout)
        self.assertIn("Sao l", proc.stdout)  # manual cloud-backup reminder

    def test_backup_empty_pull_fails(self):
        self.env["FAKE_PULL_FILES"] = "0"
        proc = self.run_script("backup-zalo-data.sh")
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("zero files", proc.stderr)

    # restore-zalo-data.sh

    def test_restore_no_backup_available(self):
        proc = self.run_script("restore-zalo-data.sh")
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("LOG IN", proc.stderr)

    def test_restore_missing_pkg_subdir(self):
        empty = self.root / "backup_zalo_20260101_000000"
        empty.mkdir()
        proc = self.run_script("restore-zalo-data.sh", str(empty))
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("com.zing.zalo", proc.stderr)

    def test_restore_happy_path_explicit(self):
        backup = self.root / "backup_zalo_20260101_000000" / "com.zing.zalo"
        backup.mkdir(parents=True)
        (backup / "photo0.jpg").write_text("x")
        proc = self.run_script("restore-zalo-data.sh", str(backup.parent))
        self.assertEqual(proc.returncode, 0, proc.stderr)
        self.assertIn("Restore complete", proc.stdout)

    def test_restore_defaults_to_latest(self):
        for stamp in ("20260101_000000", "20260202_000000"):
            pkg = self.root / f"backup_zalo_{stamp}" / "com.zing.zalo"
            pkg.mkdir(parents=True)
            (pkg / "photo0.jpg").write_text("x")
        proc = self.run_script("restore-zalo-data.sh")
        self.assertEqual(proc.returncode, 0, proc.stderr)
        self.assertIn("Restore complete", proc.stdout)

    def test_restore_no_device(self):
        backup = self.root / "backup_zalo_20260101_000000" / "com.zing.zalo"
        backup.mkdir(parents=True)
        self.env["FAKE_DEVICES"] = ""
        proc = self.run_script("restore-zalo-data.sh", str(backup.parent))
        self.assertNotEqual(proc.returncode, 0)
        self.assertIn("exactly one", proc.stderr)


if __name__ == "__main__":
    unittest.main()
