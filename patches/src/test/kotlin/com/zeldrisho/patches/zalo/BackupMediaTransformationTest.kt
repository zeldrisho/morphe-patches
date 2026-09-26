package com.zeldrisho.patches.zalo

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction11x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction3rc
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.zeldrisho.patches.testing.syntheticMutableMethod
import com.zeldrisho.patches.zalo.backup.enableMediaBackup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupMediaTransformationTest {
    /** Builds a result instruction followed by a configurable static backup-configuration write call. */
    private fun writer(
        target: String = "Lu40/p0;",
        name: String = "i0",
        resultRegister: Int = 0,
        argumentRegisters: List<Int> = listOf(0),
        parameterTypes: List<String> = listOf("Z"),
    ) = syntheticMutableMethod(
        registerCount = maxOf(resultRegister, argumentRegisters.maxOrNull() ?: 0) + 1,
        instructions = listOf(
            ImmutableInstruction11x(Opcode.MOVE_RESULT, resultRegister),
            ImmutableInstruction35c(
                Opcode.INVOKE_STATIC,
                argumentRegisters.size,
                argumentRegisters.getOrElse(0) { 0 },
                argumentRegisters.getOrElse(1) { 0 },
                argumentRegisters.getOrElse(2) { 0 },
                argumentRegisters.getOrElse(3) { 0 },
                argumentRegisters.getOrElse(4) { 0 },
                ImmutableMethodReference(target, name, parameterTypes, "V"),
            ),
        ),
    )

    /** Checks that the result before the backup write is replaced while the call remains intact. */
    @Test
    fun enablesOnlyAtBackupMediaWriteAndPreservesFollowingCall() {
        val method = writer()
        enableMediaBackup(method)
        assertEquals(
            listOf(Opcode.CONST_4, Opcode.INVOKE_STATIC),
            method.implementation!!.instructions.map { it.opcode },
        )
    }

    /** Checks that the boolean argument is selected by position after a non-wide parameter. */
    @Test
    fun selectsBooleanArgumentAfterEarlierNonWideArgument() {
        val method = writer(
            resultRegister = 1,
            argumentRegisters = listOf(0, 1),
            parameterTypes = listOf("Ljava/lang/Object;", "Z"),
        )
        enableMediaBackup(method)
        assertEquals(Opcode.CONST_4, method.implementation!!.instructions.first().opcode)
    }

    @Test
    fun supportsRangeInvokeAndWideParameterSlots() {
        val method = syntheticMutableMethod(
            registerCount = 4,
            instructions = listOf(
                ImmutableInstruction11x(Opcode.MOVE_RESULT, 3),
                ImmutableInstruction3rc(
                    Opcode.INVOKE_STATIC_RANGE,
                    1,
                    3,
                    ImmutableMethodReference("Lu40/p0;", "i0", listOf("D", "Z"), "V"),
                ),
            ),
        )
        enableMediaBackup(method)
        assertEquals(Opcode.CONST_4, method.implementation!!.instructions.first().opcode)
    }

    @Test
    fun rejectsEarlierResultUsedByAnotherArgumentInsteadOfBooleanValue() {
        val method = writer(
            argumentRegisters = listOf(1, 0),
            parameterTypes = listOf("Z", "Ljava/lang/Object;"),
        )
        val error = assertFailsWith<IllegalStateException> { enableMediaBackup(method) }
        assertEquals("Zalo Google Drive backup: parsed result does not feed ENABLE_BACKUP_MEDIA write", error.message)
    }

    /** Checks the diagnostic when the expected backup write owner is absent. */
    @Test
    fun rejectsMissingBackupWrite() {
        val error = assertFailsWith<IllegalStateException> {
            enableMediaBackup(writer(target = "Lwrong/Owner;"))
        }
        assertEquals("Zalo Google Drive backup: ENABLE_BACKUP_MEDIA write not found", error.message)
    }

    /** Checks the diagnostic when backup configuration has no bytecode implementation. */
    @Test
    fun rejectsMissingImplementation() {
        val method = syntheticMutableMethod(registerCount = 1, instructions = null)
        val error = assertFailsWith<IllegalStateException> { enableMediaBackup(method) }
        assertEquals("Zalo Google Drive backup: configuration method has no implementation", error.message)
    }
}
