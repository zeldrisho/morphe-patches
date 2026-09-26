package com.zeldrisho.patches.zalo.media

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.immutable.instruction.ImmutableInstruction21c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableFieldReference
import com.zeldrisho.patches.testing.syntheticMutableMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SendOriginalMediaSelectionTest {
    private fun field(owner: String, name: String) = ImmutableInstruction21c(
        Opcode.SGET_OBJECT,
        0,
        ImmutableFieldReference(owner, name, "Ltest/Field;"),
    )

    @Test
    fun selectionHelpersApplySingleFirstAndEarliestRules() {
        val instructions = listOf(
            field("Ltest/Owner;", "OTHER"),
            field("Ltest/Owner;", "Z1"),
            field("Ltest/Owner;", "J0"),
            field("Ltest/Owner;", "Z1"),
        )
        assertEquals(1, singleFieldInstructionIndex(instructions.take(2), "Z1"))
        assertEquals(2, firstFieldInstructionIndex(instructions, "J0"))
        assertEquals(1, earliestFieldInstructionIndex(instructions, "Z1", "missing Z1"))
        assertFailsWith<NoSuchElementException> { singleFieldInstructionIndex(instructions, "MISSING") }
        assertFailsWith<NoSuchElementException> { firstFieldInstructionIndex(instructions, "MISSING") }
        val error = assertFailsWith<IllegalStateException> {
            earliestFieldInstructionIndex(instructions, "MISSING", "target moved")
        }
        assertEquals("target moved", error.message)
    }

    @Test
    fun earliestHdPickerFieldAndMediaFlagAreReplacedInTheirDestinationRegisters() {
        val chipFields = listOf(field("Ltest/Owner;", "HD"), field("Ltest/Owner;", "Z1"), field("Ltest/Owner;", "Z1"))
        val chip = syntheticMutableMethod(
            registerCount = 2,
            instructions = chipFields,
        )
        replaceEarliestFieldInstruction(
            chip,
            chipFields,
            "Z1",
            "const/4 v1, 0x2",
            "missing Z1",
        )
        val chipInstructions = chip.implementation!!.instructions.toList()
        assertEquals(Opcode.SGET_OBJECT, chipInstructions[0].opcode)
        assertEquals(Opcode.CONST_4, chipInstructions[1].opcode)
        assertEquals(Opcode.SGET_OBJECT, chipInstructions[2].opcode)

        val mediaFields = listOf(
            field("Lcom/zing/zalo/data/mediapicker/model/MediaItem;", "q"),
            field("Lcom/zing/zalo/data/mediapicker/model/MediaItem;", "q"),
        )
        val media = syntheticMutableMethod(registerCount = 2, instructions = mediaFields)
        replaceEarliestMediaItemFlag(media, mediaFields)
        val mediaInstructions = media.implementation!!.instructions.toList()
        assertEquals(Opcode.CONST_4, mediaInstructions[0].opcode)
        assertEquals(Opcode.SGET_OBJECT, mediaInstructions[1].opcode)
    }

    @Test
    fun mediaItemOriginalFlagSelectionIsClassSpecificAndEarliest() {
        val instructions = listOf(
            field("Lother/MediaItem;", "q"),
            field("Lcom/zing/zalo/data/mediapicker/model/MediaItem;", "metadata"),
            field("Lcom/zing/zalo/data/mediapicker/model/MediaItem;", "q"),
            field("Lcom/zing/zalo/data/mediapicker/model/MediaItem;", "q"),
        )
        assertEquals(2, earliestMediaItemFlagIndex(instructions))
        val error = assertFailsWith<IllegalStateException> { earliestMediaItemFlagIndex(instructions.take(2)) }
        assertEquals("MediaItem original flag read moved; re-hunt Lbq0/g->a()", error.message)
    }
}
