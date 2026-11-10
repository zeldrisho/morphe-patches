package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourceFile
import app.morphe.patcher.util.returnEarly
import app.zeldrisho.patches.zalo.navigation.zeldrisSettingsPatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import java.io.File
import java.nio.ByteBuffer
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/**
 * Checks the PNG signature and IHDR dimensions without decoding the image.
 *
 * @throws IllegalArgumentException if the input is outside 33 bytes to 8 MiB inclusive, lacks
 * the expected header, or is not square with each dimension between 48 and 2048 pixels inclusive.
 */
internal fun validateLauncherPng(bytes: ByteArray) {
    require(bytes.size in 33..8 * 1024 * 1024) { "Icon must be a PNG smaller than 8 MiB" }
    require(bytes.copyOfRange(0, 8).contentEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)) &&
        ByteBuffer.wrap(bytes, 8, 4).int == 13 && String(bytes, 12, 4, Charsets.US_ASCII) == "IHDR"
    ) { "Icon must have a PNG IHDR header" }
    val width = ByteBuffer.wrap(bytes, 16, 4).int
    val height = ByteBuffer.wrap(bytes, 20, 4).int
    require(width == height && width in 48..2048) { "Icon must be square, 48–2048 pixels" }
}

private val customIconSettingsPatch = bytecodePatch {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    dependsOn(zeldrisSettingsPatch)
    execute {
        mutableClassDefBy("Lapp/zeldrisho/zalo/extension/ZeldrisSettingsContent;")
            .methods.single { it.name == "hasCustomIconSettings" && it.parameterTypes.isEmpty() && it.returnType == "Z" }
            .returnEarly(true)
    }
}

@Suppress("unused")
val changeZaloAppIconPatch = resourcePatch(
    name = "Change Zalo app icon",
    description = "Adds a user-supplied PNG icon and an in-app switch between the original and custom launcher icons.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    dependsOn(customIconSettingsPatch)
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
            val aliases = application.getElementsByTagName("activity-alias")
            val original = (0 until aliases.length).map { aliases.item(it) as Element }.singleOrNull {
                it.getAttributeNS(ANDROID_NS, "name") == "com.zing.zalo.ui.SplashActivity"
            } ?: error("Zalo launcher alias changed")
            check(original.getAttributeNS(ANDROID_NS, "targetActivity") == "com.zing.zalo.ui.ZaloLauncherActivity") {
                "Zalo launcher target changed"
            }
            val filters = original.getElementsByTagName("intent-filter")
            check(filters.length == 1) { "Zalo launcher intent filters changed" }
            val categories = (filters.item(0) as Element).getElementsByTagName("category")
            check((0 until categories.length).any {
                (categories.item(it) as Element).getAttributeNS(ANDROID_NS, "name") == "android.intent.category.LAUNCHER"
            }) { "Zalo launcher category changed" }
            val alternate = original.cloneNode(true) as Element
            alternate.setAttributeNS(ANDROID_NS, "android:name", "com.zing.zalo.ui.ZeldrisCustomIcon")
            alternate.setAttributeNS(ANDROID_NS, "android:enabled", "false")
            alternate.setAttributeNS(ANDROID_NS, "android:icon", "@mipmap/zeldris_launcher")
            alternate.setAttributeNS(ANDROID_NS, "android:roundIcon", "@mipmap/zeldris_launcher")
            application.appendChild(alternate)
        }
    }
}
