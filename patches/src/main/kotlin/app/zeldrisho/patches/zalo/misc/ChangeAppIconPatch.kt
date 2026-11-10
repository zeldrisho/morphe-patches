package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.patch.resourceFile
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import java.io.File
import java.nio.ByteBuffer
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/** Check the PNG header without depending on desktop-only image APIs in Android patchers. */
internal fun validateLauncherPng(bytes: ByteArray) {
    require(bytes.size in 33..8 * 1024 * 1024) { "Icon must be a PNG smaller than 8 MiB" }
    require(bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)) &&
        ByteBuffer.wrap(bytes, 8, 4).int == 13 && String(bytes, 12, 4, Charsets.US_ASCII) == "IHDR"
    ) { "Icon must have a PNG IHDR header" }
    val width = ByteBuffer.wrap(bytes, 16, 4).int
    val height = ByteBuffer.wrap(bytes, 20, 4).int
    require(width == height && width in 48..2048) { "Icon must be square, 48–2048 pixels" }
}

@Suppress("unused")
val changeZaloAppIconPatch = resourcePatch(
    name = "Change Zalo app icon",
    description = "Uses a user-supplied square PNG for the launcher and adaptive app icons.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    val iconPath by stringOption(
        key = "iconPath", title = "PNG icon path",
        description = "Path accessible to the patcher. Use a square 48–2048 px PNG, with padding for adaptive masking.",
        required = true,
    )
    execute {
        val input = File(requireNotNull(iconPath) { "Choose a PNG icon path" })
        require(input.isFile && input.length() <= 8 * 1024 * 1024) { "Icon is missing or exceeds 8 MiB" }
        val bytes = input.readBytes()
        validateLauncherPng(bytes)
        resourceFile("res/drawable-nodpi/zeldris_launcher_image.png", false).apply {
            parentFile.mkdirs()
            writeBytes(bytes)
        }
        resourceFile("res/mipmap-nodpi/zeldris_launcher.png", false).apply {
            parentFile.mkdirs()
            writeBytes(bytes)
        }
        resourceFile("res/mipmap-anydpi-v26/zeldris_launcher.xml", false).apply {
            parentFile.mkdirs()
            writeText("""
                <adaptive-icon xmlns:android="$ANDROID_NS">
                    <background android:drawable="#FFFFFFFF" />
                    <foreground android:drawable="@drawable/zeldris_launcher_image" />
                </adaptive-icon>
            """.trimIndent())
        }
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element
            application.setAttributeNS(ANDROID_NS, "android:icon", "@mipmap/zeldris_launcher")
            application.setAttributeNS(ANDROID_NS, "android:roundIcon", "@mipmap/zeldris_launcher")
            // Only launcher components: leave call, share and notification artwork alone.
            for (tag in listOf("activity", "activity-alias")) {
                val components = application.getElementsByTagName(tag)
                for (i in 0 until components.length) {
                    val component = components.item(i) as Element
                    val filters = component.getElementsByTagName("intent-filter")
                    val launcher = (0 until filters.length).any { index ->
                        val filter = filters.item(index) as Element
                        fun has(name: String, value: String): Boolean {
                            val elements = filter.getElementsByTagName(name)
                            return (0 until elements.length).any {
                                (elements.item(it) as Element).getAttributeNS(ANDROID_NS, "name") == value
                            }
                        }
                        has("action", "android.intent.action.MAIN") &&
                            has("category", "android.intent.category.LAUNCHER")
                    }
                    if (launcher) {
                        component.setAttributeNS(ANDROID_NS, "android:icon", "@mipmap/zeldris_launcher")
                        component.setAttributeNS(ANDROID_NS, "android:roundIcon", "@mipmap/zeldris_launcher")
                    }
                }
            }
        }
    }
}
