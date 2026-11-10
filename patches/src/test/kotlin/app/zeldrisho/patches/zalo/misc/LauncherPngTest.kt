package app.zeldrisho.patches.zalo.misc

import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LauncherPngTest {
    private fun header(width: Int = 512, height: Int = width): ByteArray = ByteArray(33).apply {
        byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10).copyInto(this)
        ByteBuffer.wrap(this, 8, 4).putInt(13)
        "IHDR".toByteArray(Charsets.US_ASCII).copyInto(this, 12)
        ByteBuffer.wrap(this, 16, 4).putInt(width)
        ByteBuffer.wrap(this, 20, 4).putInt(height)
    }

    @Test fun acceptsSupportedSquareDimensions() {
        for (size in listOf(48, 512, 2048)) validateLauncherPng(header(size))
    }

    @Test fun rejectsInvalidDimensions() {
        for (size in listOf(-1, 0, 47, 2049, Int.MAX_VALUE)) {
            assertFailsWith<IllegalArgumentException> { validateLauncherPng(header(size)) }
        }
        assertFailsWith<IllegalArgumentException> { validateLauncherPng(header(512, 256)) }
    }

    @Test fun rejectsMissingAndOversizedHeaders() {
        for (bytes in listOf(ByteArray(0), ByteArray(32), ByteArray(8 * 1024 * 1024 + 1))) {
            assertFailsWith<IllegalArgumentException> { validateLauncherPng(bytes) }
        }
    }

    @Test fun rejectsWrongSignatureAndChunk() {
        assertFailsWith<IllegalArgumentException> { validateLauncherPng(header().apply { this[0] = 0 }) }
        assertFailsWith<IllegalArgumentException> { validateLauncherPng(header().apply { this[15] = 0 }) }
        assertFailsWith<IllegalArgumentException> { validateLauncherPng(header().apply { this[11] = 12 }) }
    }
}
