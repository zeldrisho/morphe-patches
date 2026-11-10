package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.string
import app.morphe.patcher.util.returnEarly
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import app.zeldrisho.patches.zalo.navigation.zeldrisSettingsPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal const val AMOLED_GUARD = """
    invoke-static {p0, p1}, Lapp/zeldrisho/zalo/extension/AmoledTheme;->blackSurface(Landroid/content/Context;I)Z
    move-result v0
    if-eqz v0, :native_color
    const v0, -0x1000000
    return v0
    :native_color
    nop
"""

private val amoledOverlayResources = resourcePatch {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    execute {
        document("res/values/styles.xml").use { document ->
            val style = document.createElement("style")
            style.setAttribute("name", "ZeldrisAmoledOverlay")
            for (name in listOf("PrimaryBackgroundColor", "SecondaryBackgroundColor", "BottomNav",
                "TabBackgroundColor", "ChatBarBackgroundColor", "FooterBackgroundColor",
                "ProfilePrimaryBackgroundColor", "ProfileSecondaryBackgroundColor",
                "TimelinePrimaryBackgroundColor", "TimelineSecondaryBackgroundColor", "PopupBackgroundColor")) {
                val item = document.createElement("item")
                item.setAttribute("name", name)
                item.textContent = "#ff000000"
                style.appendChild(item)
            }
            document.documentElement.appendChild(style)
        }
    }
}

/** Hooks host theme creation and themed-color lookup; light mode remains native. */
@Suppress("unused")
val zaloAmoledThemePatch = bytecodePatch(
    name = "AMOLED dark theme",
    description = "Adds an initially off AMOLED switch in Zeldris Patches. Enable Zalo's dark theme separately; restart to apply.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    dependsOn(zeldrisSettingsPatch, amoledOverlayResources)
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
        // Apply after the native dark setTheme, before any dependent views are inflated.
        // The native light branch uses the separate setTheme(v6,p1) call and is untouched.
        val setup = ThemeSetup261002.matchAll(1..1).single().method
        val setupInstructions = setup.implementation!!.instructions.toList()
        check(setup.implementation!!.registerCount == 13) { "Zalo theme setup registers changed" }
        val darkCalls = setupInstructions.indices.filter { index ->
            val instruction = setupInstructions[index]
            val invoke = instruction as? FiveRegisterInstruction
            instruction.opcode == Opcode.INVOKE_VIRTUAL &&
                (instruction as? ReferenceInstruction)?.reference?.toString() ==
                    "Landroid/content/Context;->setTheme(I)V" &&
                invoke?.registerCount == 2 && invoke.registerC == 6 && invoke.registerD == 9
        }
        check(darkCalls.size == 1) { "Zalo dark-theme setTheme call changed" }
        setup.addInstructions(darkCalls.single() + 1,
            "invoke-static {v6}, Lapp/zeldrisho/zalo/extension/AmoledTheme;->applyDarkOverlay(Landroid/content/Context;)V")
        // v0 is not live at entry; p0/p1 are v3/v4. Native lookup remains intact on false.
        method.addInstructionsWithLabels(0, AMOLED_GUARD)
    }
}

private object ThemeSetup261002 : Fingerprint(
    definingClass = "Lef1/c;", name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("I", "I", "Ljava/util/ArrayList;", "Z"), returnType = "V",
    filters = listOf(string("SAVE_SETTING_THEME_SELECTED_POSITION"),
        methodCall(definingClass = "Landroid/content/Context;", name = "setTheme", parameters = listOf("I"), returnType = "V")),
)

private object ThemeColor261002 : Fingerprint(
    definingClass = "Lef1/c;", name = "g",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "I"), returnType = "I",
    filters = listOf(methodCall(definingClass = "Lp60/p0;", name = "C",
        parameters = listOf("Landroid/content/Context;", "I"), returnType = "I")),
)
