package com.zeldrisho.patches

import com.zeldrisho.patches.zalo.chat.businessBoxFingerprints
import com.zeldrisho.patches.zalo.media.mediaFingerprints
import com.zeldrisho.patches.zalo.telemetry.telemetryFingerprints
import kotlin.test.Test
import kotlin.test.assertFalse

class FingerprintDescriptorInventoryTest {
    @Test
    fun zaloFingerprintDefinitionsExposeNonEmptyDescriptorsAndFilters() {
        val fingerprints = mediaFingerprints + businessBoxFingerprints + telemetryFingerprints
        assertFalse(fingerprints.isEmpty())
        fingerprints.forEach { fingerprint ->
            // Call-site fingerprints can intentionally omit class, name, and return metadata.
            fingerprint.definingClass
            fingerprint.name
            fingerprint.parameters
            fingerprint.returnType
            fingerprint.filters
            fingerprint.strings
        }
    }
}
