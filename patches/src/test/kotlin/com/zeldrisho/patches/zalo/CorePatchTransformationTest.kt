package com.zeldrisho.patches.zalo

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction10x
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.zeldrisho.patches.testing.syntheticMutableMethod
import com.zeldrisho.patches.zalo.chat.suppressBusinessBoxInsertion
import com.zeldrisho.patches.zalo.chat.suppressBusinessBoxPeriodicBranch
import com.zeldrisho.patches.zalo.privacy.suppressSeenStatus
import com.zeldrisho.patches.zalo.privacy.suppressTypingStatus
import com.zeldrisho.patches.zalo.telemetry.disableNativeCrashHandler
import com.zeldrisho.patches.zalo.telemetry.forceReturnInt
import com.zeldrisho.patches.zalo.telemetry.forceReturnVoid
import kotlin.test.Test
import kotlin.test.assertEquals

/** Exercises production bytecode transformations against synthetic mutable methods. */
class CorePatchTransformationTest {
    private fun method(returnType: String = "V", registerCount: Int = 1) = syntheticMutableMethod(
        returnType = returnType,
        registerCount = registerCount,
        instructions = listOf(
            ImmutableInstruction10x(Opcode.NOP),
            ImmutableInstruction10x(Opcode.RETURN_VOID),
        ),
    )

    @Test
    fun seenSuppressionReplacesWholeBodyWithReturnVoid() {
        val target = method()
        suppressSeenStatus(target)
        assertEquals(listOf(Opcode.RETURN_VOID), target.implementation!!.instructions.map { it.opcode })
    }

    @Test
    fun typingSuppressionReplacesWholeBodyWithReturnVoid() {
        val target = method()
        suppressTypingStatus(target)
        assertEquals(listOf(Opcode.RETURN_VOID), target.implementation!!.instructions.map { it.opcode })
    }

    @Test
    fun businessBoxInsertionAndPeriodicBranchReturnAtSelectedInstruction() {
        val insertion = method()
        suppressBusinessBoxInsertion(insertion, 0)
        assertEquals(Opcode.RETURN_VOID, insertion.implementation!!.instructions[0].opcode)

        val periodic = method()
        suppressBusinessBoxPeriodicBranch(periodic, 0)
        assertEquals(Opcode.RETURN_VOID, periodic.implementation!!.instructions[0].opcode)
    }

    @Test
    fun nativeCrashRegistrationCallIsNoppedWithoutRemovingAdjacentInstructions() {
        val target = syntheticMutableMethod(
            registerCount = 1,
            instructions = listOf(
                ImmutableInstruction35c(
                    Opcode.INVOKE_STATIC,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    ImmutableMethodReference("Ltest/NativeCrash;", "init", emptyList(), "V"),
                ),
                ImmutableInstruction10x(Opcode.RETURN_VOID),
            ),
        )
        disableNativeCrashHandler(target, 0)
        assertEquals(
            listOf(Opcode.NOP, Opcode.RETURN_VOID),
            target.implementation!!.instructions.map { it.opcode },
        )
    }

    @Test
    fun telemetryVoidSinksBecomeNoOpsAndIntegerSinksReturnZero() {
        val voidSink = method()
        forceReturnVoid(voidSink)
        assertEquals(listOf(Opcode.RETURN_VOID), voidSink.implementation!!.instructions.map { it.opcode })

        val intSink = method(returnType = "I")
        forceReturnInt(intSink)
        assertEquals(
            listOf(Opcode.CONST_4, Opcode.RETURN),
            intSink.implementation!!.instructions.map { it.opcode },
        )
    }
}
