"""Unit tests for the offline Markdown-link checker."""

import unittest

from scripts import check_markdown_links


class MarkdownLinkTest(unittest.TestCase):
    def test_github_style_heading_slug(self):
        self.assertEqual(
            check_markdown_links.slug("Signing (keystore flags need `=`)"),
            "signing-keystore-flags-need",
        )

    def test_formatting_is_removed_from_slug(self):
        self.assertEqual(check_markdown_links.slug("**Build** & test"), "build-test")


if __name__ == "__main__":
    unittest.main()
