package com.zeldrisho.patches.zalo.media

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO

private const val SMALI_HEX_RADIX = 16

private fun fieldName(instruction: Instruction): String? = ((instruction as? ReferenceInstruction)?.reference as? FieldReference)?.name

internal fun singleFieldInstructionIndex(instructions: List<Instruction>, name: String): Int = instructions.indices.single { fieldName(instructions[it]) == name }

internal fun firstFieldInstructionIndex(instructions: List<Instruction>, name: String): Int = instructions.indices.first { fieldName(instructions[it]) == name }

internal fun earliestFieldInstructionIndex(
    instructions: List<Instruction>,
    name: String,
    missingMessage: String,
): Int = instructions.indices.filter { fieldName(instructions[it]) == name }.minOrNull()
    ?: error(missingMessage)

internal fun earliestMediaItemFlagIndex(instructions: List<Instruction>): Int = instructions.indices.filter { index ->
    val reference = (instructions[index] as? ReferenceInstruction)?.reference as? FieldReference
    reference?.definingClass == "Lcom/zing/zalo/data/mediapicker/model/MediaItem;" && reference.name == "q"
}.minOrNull() ?: error("MediaItem original flag read moved; re-hunt Lbq0/g->a()")

internal fun replaceFieldInstruction(method: MutableMethod, index: Int, replacement: String) {
    method.replaceInstruction(index, replacement)
}

internal fun replaceEarliestFieldInstruction(
    method: MutableMethod,
    instructions: List<Instruction>,
    name: String,
    replacement: String,
    missingMessage: String,
) {
    replaceFieldInstruction(method, earliestFieldInstructionIndex(instructions, name, missingMessage), replacement)
}

internal fun replaceEarliestMediaItemFlag(method: MutableMethod, instructions: List<Instruction>) {
    replaceFieldInstruction(
        method,
        earliestMediaItemFlagIndex(instructions),
        "const/4 v13, 0x1",
    )
}

internal fun forceQualityResult(method: app.morphe.patcher.util.proxy.mutableTypes.MutableMethod, value: Int) {
    method.addInstructions(0, "const/4 v0, 0x${value.toString(SMALI_HEX_RADIX)}\nreturn v0")
}

internal fun forcePickerQualityArgument(method: app.morphe.patcher.util.proxy.mutableTypes.MutableMethod) {
    method.addInstructions(0, "const/4 p0, 0x2")
}

/** Enables Zalo's existing server-supported original-quality photo path. */
@Suppress("unused")
val sendZaloOriginalMediaPatch = bytecodePatch(
    name = "Prefer original photo quality",
    description = "Enables Zalo's existing original-quality photo path by default. " +
        "It does not change server upload limits, account restrictions, or video handling.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        forceQualityResult(SelectedMediaQuality.method, 2)

        // The quality sheet receives the current selection in a Bundle. Force
        // that initial value too; otherwise the sheet can still open on HD
        // when the stored selection predates this patch.
        forcePickerQualityArgument(QualityPickerArguments.method)

        // MediaPickerView.b7() initializes the photo picker to HD when the
        // quality control is enabled. Change only that initialization; the
        // non-HD branch remains Standard.
        val defaultQualityIndex = singleFieldInstructionIndex(
            PickerQualityInitialization.instructionMatches.map { it.instruction },
            "HD",
        )
        PickerQualityInitialization.method.replaceInstruction(
            defaultQualityIndex,
            "sget-object v0, Lvh1/d;->ORIGINAL:Lvh1/d;",
        )

        // Keep the quality chip consistent with the forced outgoing choice.
        PhotoQualityChipUpdate.method.addInstructions(
            0,
            """
            const/4 p1, 0x2
            """.trimIndent(),
        )

        // After selection, the landing page refreshes its own chip from Z1.
        // Override only that cached photo-quality value; visibility and video
        // handling remain unchanged.
        val landingPageQualityIndex = singleFieldInstructionIndex(
            LandingPageQualityChipUpdate.instructionMatches.map { it.instruction },
            "Z1",
        )
        LandingPageQualityChipUpdate.method.replaceInstruction(
            landingPageQualityIndex,
            "const/4 v1, 0x2",
        )

        // The send-mode layout initializes the same chip from Z1 before the
        // selection callback runs. Force that label as well; leave the later
        // HD-checkbox initialization untouched.
        replaceEarliestFieldInstruction(
            LandingPageQualityChipInitialization.method,
            LandingPageQualityChipInitialization.instructionMatches.map { it.instruction },
            "Z1",
            "const/4 p3, 0x2",
            "LandingPageView quality-chip initialization moved; re-hunt W4()",
        )

        // The chat input bar also mirrors the picker quality after selection.
        // This is the visible chip in the normal send flow.
        val chatInputBarQualityIndex = firstFieldInstructionIndex(
            ChatInputBarQualityChipUpdate.instructionMatches.map { it.instruction },
            "J0",
        )
        ChatInputBarQualityChipUpdate.method.replaceInstruction(
            chatInputBarQualityIndex,
            "const/4 v0, 0x2",
        )

        // Some selection callbacks update the chip through a path that does
        // not pass through the three owners above. Enforce the label at the
        // quality-chip rendering boundary; this widget is not used by video
        // sending, whose controls use separate views.
        QualityChipLabel.method.addInstructions(
            0,
            """
            const/4 v0, 0x2
            invoke-static {v0}, Lvh1/c;->a(I)Ljava/lang/String;
            move-result-object p1
            """.trimIndent(),
        )

        // The send conversion copies MediaItem.q into the outgoing photo
        // model. The picker UI can display Original while this flag remains
        // false, which causes the upload to use HD. Change only that copy;
        // the other q read feeds metadata and is intentionally untouched.
        replaceEarliestMediaItemFlag(
            SelectedPhotoOriginalFlag.method,
            SelectedPhotoOriginalFlag.instructionMatches.map { it.instruction },
        )

        // The picker checks these helpers directly before it calls e(). In
        // 26.08.01, f() is the account/config entitlement check that sends a
        // non-entitled selection into the Z Cloud purchase flow. Patching only
        // e() makes the option visible but still leaves that flow reachable.
        forceQualityResult(OriginalMediaQualityEnabled.method, 1)
        forceQualityResult(OriginalMediaQualityEntitled.method, 1)
        forceQualityResult(OriginalMediaQualityAvailable.method, 1)
    }
}
