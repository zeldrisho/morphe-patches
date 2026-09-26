package com.zeldrisho.patches

import com.google.gson.JsonParser
import util.JsonPatch
import util.formatPatchList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PatchListGeneratorTest {
    @Test
    fun formatsPatchMetadataAsValidatedPrettyJson() {
        val json = formatPatchList(
            "2.3.4",
            listOf(
                JsonPatch(
                    name = "Synthetic patch",
                    description = "Keeps <content> intact",
                    default = false,
                    dependencies = listOf("BasePatch"),
                    options = listOf(
                        JsonPatch.Option(
                            key = "enabled",
                            title = "Enable feature",
                            description = null,
                            required = true,
                            type = "Boolean",
                            default = true,
                            values = null,
                        ),
                    ),
                ),
            ),
        )

        val root = JsonParser.parseString(json).asJsonObject
        assertEquals("2.3.4", root.get("version").asString)
        assertTrue(root.get("NOTE").asString.contains("Do NOT manually edit"))
        val patch = root.getAsJsonArray("patches").single().asJsonObject
        assertEquals("Synthetic patch", patch.get("name").asString)
        assertEquals("Keeps <content> intact", patch.get("description").asString)
        assertEquals("BasePatch", patch.getAsJsonArray("dependencies").single().asString)
        assertEquals("enabled", patch.getAsJsonArray("options").single().asJsonObject.get("key").asString)
    }

    @Test
    fun rejectsInvalidMetadataBeforeReturningJson() {
        assertFailsWith<IllegalStateException> {
            formatPatchList(
                "2.3.4",
                listOf(JsonPatch(name = "", dependencies = emptyList(), options = emptyList())),
            )
        }
    }
}
