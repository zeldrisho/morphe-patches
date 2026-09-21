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
    fun clearBodyRemovesAllInstructionsAndExceptionRanges() {
        val target = method()
        val implementation = target.implementation!!
        val start = implementation.newLabelForIndex(0)
        val end = implementation.newLabelForIndex(1)
        val handler = implementation.newLabelForIndex(0)
        implementation.addCatch("Ljava/lang/Exception;", start, end, handler)
        assertTrue(implementation.tryBlocks.isNotEmpty())

        target.clearBody()

        assertTrue(implementation.instructions.none())
        assertTrue(implementation.tryBlocks.none())
    }

    @Test
    fun registerGrowthIsAvailableForWholeBodyReplacement() {
        val target = method(1)
        target.ensureRegisters(4)
        target.ensureRegisters(2)
        assertEquals(4, target.implementation!!.registerCount)
        target.clearBody()
        assertEquals(0, target.implementation!!.instructions.size)
    }
}
