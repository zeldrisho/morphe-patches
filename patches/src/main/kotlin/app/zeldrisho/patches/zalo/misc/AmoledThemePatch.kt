package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.returnEarly
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import app.zeldrisho.patches.zalo.navigation.zeldrisSettingsPatch
import com.android.tools.smali.dexlib2.AccessFlags

internal const val AMOLED_GUARD = """
    invoke-static {p0, p1}, Lapp/zeldrisho/zalo/extension/AmoledTheme;->blackSurface(Landroid/content/Context;I)Z
    move-result v0
    if-eqz v0, :native_color
    const v0, -0x1000000
    return v0
    :native_color
    nop
"""

/** Hooks the host's themed-color lookup; the user's dark-mode choice remains native. */
@Suppress("unused")
val zaloAmoledThemePatch = bytecodePatch(
    name = "AMOLED dark theme",
    description = "Adds an initially off AMOLED switch in Zeldris Patches. Enable Zalo's dark theme separately; restart to apply.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    dependsOn(zeldrisSettingsPatch)
    execute {
        val method = ThemeColor261002.matchAll(1..1).single().method
        check(method.implementation!!.registerCount == 5) { "Zalo themed color lookup register layout changed" }
        val dark = classDefBy("Lef1/c;").methods.singleOrNull {
            it.name == "d" && it.parameterTypes == listOf("Landroid/content/Context;") && it.returnType == "Z" &&
                AccessFlags.PUBLIC.isSet(it.accessFlags) && AccessFlags.STATIC.isSet(it.accessFlags)
        }
        check(dark != null) { "Zalo context-aware dark theme detector changed" }
        mutableClassDefBy("Lapp/zeldrisho/zalo/extension/ZeldrisSettingsContent;")
            .methods.single { it.name == "hasAmoledSettings" && it.parameterTypes.isEmpty() && it.returnType == "Z" }
            .returnEarly(true)
        // v0 is not live at entry; p0/p1 are v3/v4. Native lookup remains intact on false.
        method.addInstructionsWithLabels(0, AMOLED_GUARD)
    }
}

private object ThemeColor261002 : Fingerprint(
    definingClass = "Lef1/c;", name = "g",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "I"), returnType = "I",
    filters = listOf(methodCall(definingClass = "Lp60/p0;", name = "C",
        parameters = listOf("Landroid/content/Context;", "I"), returnType = "I")),
)
