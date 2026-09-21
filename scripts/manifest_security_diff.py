"""Compare security-relevant Android manifest boundaries without APKs."""

import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"


def _value(element, name):
    return element.get(ANDROID + name, "")


def security_snapshot(xml_text):
    root = ET.fromstring(xml_text)
    permissions = sorted(
        element.get(ANDROID + "name", "")
        for element in root.findall("permission") + root.findall("uses-permission")
    )
    components = []
    for tag in ("activity", "activity-alias", "service", "receiver", "provider"):
        for element in root.findall("application/" + tag):
            components.append(
                (
                    tag,
                    _value(element, "name"),
                    _value(element, "exported"),
                    _value(element, "permission"),
                    _value(element, "authorities"),
                    _value(element, "grantUriPermissions"),
                )
            )
    queries = sorted(
        element.get(ANDROID + "package", "")
        for element in root.findall("queries/package")
    )
    return {
        "permissions": permissions,
        "components": sorted(components),
        "queries_packages": queries,
    }


def security_delta(original_xml, patched_xml):
    original = security_snapshot(original_xml)
    patched = security_snapshot(patched_xml)
    return {
        key: {
            "removed": sorted(set(original[key]) - set(patched[key])),
            "added": sorted(set(patched[key]) - set(original[key])),
        }
        for key in original
    }
