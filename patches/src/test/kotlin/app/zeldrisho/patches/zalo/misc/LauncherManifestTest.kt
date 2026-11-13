package app.zeldrisho.patches.zalo.misc

import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.w3c.dom.Element

class LauncherManifestTest {
    private val androidNs = "http://schemas.android.com/apk/res/android"

    private fun application(): Element = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder().parse("""
        <manifest xmlns:android="$androidNs">
            <application android:icon="@mipmap/stock" android:roundIcon="@mipmap/stock_round">
                <activity android:name="com.zing.zalo.ui.ZaloLauncherActivity" android:icon="@drawable/override" />
                <activity android:name="other.Activity" android:icon="@drawable/unrelated" />
                <activity-alias android:name="share.Alias" android:targetActivity="com.zing.zalo.ui.ZaloLauncherActivity" />
            </application>
        </manifest>
    """.trimIndent().byteInputStream()).getElementsByTagName("application").item(0) as Element

    @Test fun replacesApplicationAndLauncherOverridesOnly() {
        val app = application()
        replaceApplicationIcons(app, "com.zing.zalo.ui.ZaloLauncherActivity")
        val launcher = app.getElementsByTagName("activity").item(0) as Element
        for (component in listOf(app, launcher)) {
            assertEquals("@mipmap/zeldris_launcher", component.getAttributeNS(androidNs, "icon"))
            assertEquals("@mipmap/zeldris_launcher", component.getAttributeNS(androidNs, "roundIcon"))
        }
        val other = app.getElementsByTagName("activity").item(1) as Element
        assertEquals("@drawable/unrelated", other.getAttributeNS(androidNs, "icon"))
        val alias = app.getElementsByTagName("activity-alias").item(0) as Element
        assertEquals("", alias.getAttributeNS(androidNs, "icon"))
    }

    @Test fun rejectsMissingOrAmbiguousLauncherBeforeChangingIcons() {
        for (duplicate in listOf(false, true)) {
            val app = application()
            if (duplicate) app.appendChild(app.getElementsByTagName("activity").item(0).cloneNode(true))
            assertFailsWith<IllegalStateException> {
                replaceApplicationIcons(app, if (duplicate) "com.zing.zalo.ui.ZaloLauncherActivity" else "missing.Activity")
            }
            assertEquals("@mipmap/stock", app.getAttributeNS(androidNs, "icon"))
        }
    }
}
