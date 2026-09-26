package com.zeldrisho.patches.zalo.backup

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO

private const val WIDE_ARGUMENT_REGISTER_WIDTH = 2
private const val REGISTER_E_ARGUMENT_INDEX = 2
private const val REGISTER_F_ARGUMENT_INDEX = 3
private const val REGISTER_G_ARGUMENT_INDEX = 4

/**
 * Enables the backup-media boolean only when the selected result feeds the matching write argument.
 *
 * Fails if the method body, write call, boolean argument, or matching result cannot be found.
 */
internal fun enableMediaBackup(method: app.morphe.patcher.util.proxy.mutableTypes.MutableMethod) {
    val implementation = method.implementation
        ?: error("Zalo Google Drive backup: configuration method has no implementation")
    val instructions = implementation.instructions
    val writeIndex = instructions.indexOfFirst { instruction ->
        if (instruction.opcode != Opcode.INVOKE_STATIC && instruction.opcode != Opcode.INVOKE_STATIC_RANGE) {
            return@indexOfFirst false
        }
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
        reference?.definingClass == "Lu40/p0;" && reference.name == "i0"
    }
    check(writeIndex >= 0) {
        "Zalo Google Drive backup: ENABLE_BACKUP_MEDIA write not found"
    }
    val writeInstruction = instructions[writeIndex]
    val writeReference = (writeInstruction as? ReferenceInstruction)?.reference as? MethodReference
        ?: error("Zalo Google Drive backup: ENABLE_BACKUP_MEDIA write reference not found")
    val booleanParameters = writeReference.parameterTypes.withIndex().filter { it.value == "Z" }
    check(booleanParameters.size == 1) {
        "Zalo Google Drive backup: expected exactly one boolean argument at ENABLE_BACKUP_MEDIA write"
    }

    // Invoke register lists count words, so wide parameters occupy two slots.
    val booleanArgumentRegisterIndex = booleanParameters.single().index.let { parameterIndex ->
        writeReference.parameterTypes.take(parameterIndex).sumOf { type ->
            if (type == "J" || type == "D") WIDE_ARGUMENT_REGISTER_WIDTH else 1
        }
    }
    val backupValueRegister = invokeRegisterAt(writeInstruction, booleanArgumentRegisterIndex)
        ?: error("Zalo Google Drive backup: boolean argument register not found")

    val resultIndex = (writeIndex - 1 downTo 0).firstOrNull { index ->
        instructions[index].opcode == Opcode.MOVE_RESULT
    }
    check(resultIndex != null) {
        "Zalo Google Drive backup: parsed backup-media result not found"
    }

    // Only replace a result that is actually consumed as the write's boolean value.
    // This avoids changing an unrelated result that happens to precede the invocation.
    val resultRegister = (instructions[resultIndex] as? OneRegisterInstruction)?.registerA
        ?: error("Zalo Google Drive backup: parsed result register not found")
    check(resultRegister == backupValueRegister) {
        "Zalo Google Drive backup: parsed result does not feed ENABLE_BACKUP_MEDIA write"
    }
    method.replaceInstruction(resultIndex, "const/4 v$resultRegister, 0x1")
}

private fun invokeRegisterAt(instruction: Instruction, registerIndex: Int): Int? = when (instruction) {
    is FiveRegisterInstruction -> if (registerIndex >= instruction.registerCount) {
        null
    } else {
        when (registerIndex) {
            0 -> instruction.registerC
            1 -> instruction.registerD
            REGISTER_E_ARGUMENT_INDEX -> instruction.registerE
            REGISTER_F_ARGUMENT_INDEX -> instruction.registerF
            REGISTER_G_ARGUMENT_INDEX -> instruction.registerG
            else -> null
        }
    }

    is RegisterRangeInstruction ->
        if (registerIndex < instruction.registerCount) instruction.startRegister + registerIndex else null

    else -> null
}

/** Enables the existing Google Drive photo-backup entry point. */
@Suppress("unused")
val enableZaloGoogleDriveMediaBackupPatch = bytecodePatch(
    name = "Enable Google Drive photo backup",
    description = "Enables Zalo's existing Google Drive photo-backup option. " +
        "It does not bypass Google authorization, server retention, encryption, or media exclusions.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        enableMediaBackup(BackupConfiguration.method)
    }
}
