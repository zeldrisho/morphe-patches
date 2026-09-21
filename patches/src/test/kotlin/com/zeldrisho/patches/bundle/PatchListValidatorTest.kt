package com.zeldrisho.patches.bundle

import kotlin.test.Test
import kotlin.test.assertFailsWith
import util.PatchListValidator

class PatchListValidatorTest {
    @Test
    fun validMinimalMetadataIsAccepted() {
        PatchListValidator.validate(validPatch())
    }

    @Test
    fun invalidTargetSdkIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            PatchListValidator.validate(validPatch().replace("\"minSdk\":23", "\"minSdk\":0"))
        }
    }

    @Test
    fun duplicateOptionsAreRejected() {
        assertFailsWith<IllegalStateException> {
            PatchListValidator.validate(validPatch().replace("\"options\":[{\"key\":\"enabled\",\"title\":\"Enabled\",\"type\":\"BOOLEAN\",\"required\":false}]", "\"options\":[{\"key\":\"enabled\",\"title\":\"Enabled\",\"type\":\"BOOLEAN\",\"required\":false},{\"key\":\"enabled\",\"title\":\"Again\",\"type\":\"BOOLEAN\",\"required\":false}]"))
        }
    }

    private fun validPatch() = """
        {"version":"1.0.0","patches":[{
          "name":"Example","default":true,
          "options":[{"key":"enabled","title":"Enabled","type":"BOOLEAN","required":false}],
          "compatiblePackages":[{"packageName":"com.example.app","targets":[{"version":"1","minSdk":23}]}]
        }]}
    """.trimIndent()
}
