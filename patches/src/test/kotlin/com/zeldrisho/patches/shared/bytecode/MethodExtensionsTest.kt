package com.zeldrisho.patches.shared.bytecode

import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Contracts for the two deliberately different method-body operations. */
class MethodExtensionsTest {
    private fun method(registers: Int = 1) = ImmutableMethod(
        "Ltest/Target;",
        "run",
        emptyList(),
        "V",
        AccessFlags.PUBLIC.value,
        emptySet(),
        emptySet(),
        ImmutableMethodImplementation(
            registers,
            listOf(ImmutableInstruction10x(Opcode.NOP)),
            emptyList(),
            emptyList(),
        ),
    ).toMutable()

    @Test
    fun clearBodyRemovesAllInstructions() {
        val target = method()
        target.clearBody()
        assertTrue(target.implementation!!.instructions.none())
    }

    @Test
    fun registerGrowthIsAvailableForWholeBodyReplacement() {
        val target = method(1)
        target.ensureRegisters(4)
        assertEquals(4, target.implementation!!.registerCount)
        target.clearBody()
        assertEquals(0, target.implementation!!.instructions.size)
    }
}
