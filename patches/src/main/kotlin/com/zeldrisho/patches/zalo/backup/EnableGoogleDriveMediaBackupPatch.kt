package com.zeldrisho.patches.zalo.backup

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO

internal fun enableMediaBackup(method: app.morphe.patcher.util.proxy.mutableTypes.MutableMethod) {
    val implementation = method.implementation
        ?: error("Zalo Google Drive backup: configuration method has no implementation")
    val writeIndex = implementation.instructions.indexOfFirst { instruction ->
        if (instruction.opcode != Opcode.INVOKE_STATIC) return@indexOfFirst false
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
        reference?.definingClass == "Lu40/p0;" && reference.name == "i0"
    }
    check(writeIndex >= 0) {
        "Zalo Google Drive backup: ENABLE_BACKUP_MEDIA write not found"
    }

    // Override only the value written to the local feature gate; other backup behavior remains.
    method.addInstructions(writeIndex, "const/4 v0, 0x1")
}

/** Enables the existing Google Drive photo-backup entry point. */
@Suppress("unused")
val enableZaloGoogleDriveMediaBackupPatch = bytecodePatch(
    name = "Enable Google Drive photo backup",
    description = "Enables Zalo's existing Google Drive photo-backup option. " +
        "It does not bypass Google authorization, server retention, encryption, or media exclusions.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        enableMediaBackup(BackupConfiguration.method)
    }
}
