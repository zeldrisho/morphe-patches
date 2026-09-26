package com.zeldrisho.patches.zalo

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction11x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.zeldrisho.patches.testing.syntheticMutableMethod
import com.zeldrisho.patches.zalo.backup.enableMediaBackup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupMediaTransformationTest {
    private fun writer(target: String = "Lu40/p0;", name: String = "i0") = syntheticMutableMethod(
        registerCount = 1,
        instructions = listOf(
            ImmutableInstruction11x(Opcode.MOVE_RESULT, 0),
            ImmutableInstruction35c(
                Opcode.INVOKE_STATIC,
                0,
                0,
                0,
                0,
                0,
                0,
                ImmutableMethodReference(target, name, emptyList(), "V"),
            ),
        ),
    )

    @Test
    fun enablesOnlyAtBackupMediaWriteAndPreservesFollowingCall() {
        val method = writer()
        enableMediaBackup(method)
        assertEquals(
            listOf(Opcode.CONST_4, Opcode.INVOKE_STATIC),
            method.implementation!!.instructions.map { it.opcode },
        )
    }

    @Test
    fun rejectsMissingBackupWrite() {
        val error = assertFailsWith<IllegalStateException> {
            enableMediaBackup(writer(target = "Lwrong/Owner;"))
        }
        assertEquals("Zalo Google Drive backup: ENABLE_BACKUP_MEDIA write not found", error.message)
    }

    @Test
    fun rejectsMissingImplementation() {
        val method = syntheticMutableMethod(registerCount = 1, instructions = null)
        val error = assertFailsWith<IllegalStateException> { enableMediaBackup(method) }
        assertEquals("Zalo Google Drive backup: configuration method has no implementation", error.message)
    }
}
