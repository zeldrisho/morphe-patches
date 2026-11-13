package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import app.zeldrisho.patches.zalo.navigation.zeldrisSettingsPatch
import com.android.tools.smali.dexlib2.AccessFlags

/** Overrides a limited set of theme-attribute lookups; not a complete app recolor. */
@Suppress("unused")
val zaloAccentColorPatch = bytecodePatch(
    name = "Customize Zalo accent color (partial)",
    description = "Adds preset colors for selected themed accent attributes; other colors may remain unchanged.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    dependsOn(zeldrisSettingsPatch)
    execute {
        val method = AccentThemeColor.matchAll(1..1).single().method
        check(method.implementation?.registerCount == 5) { "Zalo themed color lookup changed" }
        val capability = mutableClassDefBy("Lapp/zeldrisho/zalo/extension/ZeldrisSettingsContent;")
            .methods.single { it.name == "hasAccentColorSettings" && it.parameterTypes.isEmpty() && it.returnType == "Z" }
        capability.returnEarly(true)
        method.addInstructionsWithLabels(0, """
            invoke-static {p0, p1}, Lapp/zeldrisho/zalo/extension/AccentColor;->resolve(Landroid/content/Context;I)I
            move-result v0
            if-eqz v0, :native_accent
            return v0
            :native_accent
            nop
        """.trimIndent())
    }
}

private object AccentThemeColor : Fingerprint(
    definingClass = "Lef1/c;", name = "g",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "I"), returnType = "I",
    filters = listOf(methodCall(definingClass = "Lp60/p0;", name = "C",
        parameters = listOf("Landroid/content/Context;", "I"), returnType = "I")),
)
