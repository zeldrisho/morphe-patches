package com.zeldrisho.threads.patches.misc.packagename

import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun parseManifest(xml: String): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(ByteArrayInputStream(xml.toByteArray()))

private fun manifest(
    packageName: String = "com.instagram.barcelona",
    extra: String = "",
): Document = parseManifest(
    """<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="$packageName">
      <uses-permission android:name="android.permission.INTERNET"/>
      <permission android:name="com.instagram.barcelona.permission.MY_PERM"/>
      <uses-permission android:name="com.instagram.barcelona.permission.MY_PERM"/>
      <uses-permission android:name="com.google.android.gms.permission.AD_ID"/>
      <application><activity android:name="com.instagram.barcelona.mainactivity.BarcelonaActivity"/></application>
      <provider android:authorities="com.instagram.barcelona.fileprovider"/>
      <provider android:authorities="com.google.firebase.MESSAGING"/>
      $extra
    </manifest>""",
)

private fun attr(doc: Document, tag: String, attr: String): List<String> {
    val nodes = doc.getElementsByTagName(tag)
    return (0 until nodes.length).map {
        (nodes.item(it) as org.w3c.dom.Element).getAttribute(attr)
    }
}

class PackageRewriteTest {
    @Test fun validNamesAccepted() {
        assertTrue(isValidPackageName("com.instagram.barcelona.morphe"))
        assertTrue(isValidPackageName("a.b"))
    }

    @Test fun invalidNamesRejected() {
        assertFalse(isValidPackageName(null))
        assertFalse(isValidPackageName("SingleSegment"))
        assertFalse(isValidPackageName("1com.bad.name"))
        assertFalse(isValidPackageName("Com.Upper.Start"))
        assertFalse(isValidPackageName(""))
    }

    @Test fun rewritesPackageProvidersAndCustomPermissions() {
        val doc = manifest()
        rewritePackage(doc, "com.instagram.barcelona.morphe")
        assertEquals("com.instagram.barcelona.morphe", doc.documentElement.getAttribute("package"))
        assertTrue(attr(doc, "provider", "android:authorities")
            .contains("com.instagram.barcelona.morphe.fileprovider"))
        assertTrue(attr(doc, "provider", "android:authorities")
            .contains("com.google.firebase.MESSAGING"))
        val perms = attr(doc, "permission", "android:name") +
            attr(doc, "uses-permission", "android:name")
        assertTrue(perms.contains("com.instagram.barcelona.morphe.permission.MY_PERM"))
        assertFalse(perms.any { it.startsWith("com.instagram.barcelona.permission.") })
        // third-party / system permissions untouched
        assertTrue(perms.contains("android.permission.INTERNET"))
        assertTrue(perms.contains("com.google.android.gms.permission.AD_ID"))
    }
}
