package com.zeldrisho.patches.zalo.native

import app.morphe.patcher.patch.rawResourcePatch
import com.zeldrisho.patches.zalo.shared.Constants.COMPATIBILITY_ZALO
import java.io.RandomAccessFile

private const val LIBRARY_PATH = "lib/arm64-v8a/libnative_utils.so"
private const val PATCH_OFFSET = 0x28160L
private val ORIGINAL_CALL = byteArrayOf(0xa9.toByte(), 0x10, 0x00, 0x94.toByte())
private val NOP = byteArrayOf(0x1f, 0x20, 0x03, 0xd5.toByte())

/**
 * Removes the native startup tamper-exit dispatch while preserving JNI/TLS setup.
 *
 * This is intentionally pinned to Zalo 26.08.01 and the arm64 native library.
 * The surrounding bytes and original BL opcode are checked before mutation so a
 * changed native binary fails closed rather than receiving an offset patch.
 */
@Suppress("unused")
val bypassZaloNativeStartupTamperPatch = rawResourcePatch(
    name = "Bypass Zalo native startup tamper check",
    description = "Preserves Zalo native startup initialization while disabling the " +
        "re-signing exit dispatch for the pinned arm64 26.08.01 build.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZALO)

    execute {
        val library = get(LIBRARY_PATH, true)
        RandomAccessFile(library, "rw").use { file ->
            file.seek(PATCH_OFFSET)
            val actual = ByteArray(ORIGINAL_CALL.size)
            file.readFully(actual)
            check(actual.contentEquals(ORIGINAL_CALL)) {
                "Unexpected libnative_utils.so bytes at 0x${PATCH_OFFSET.toString(16)}"
            }
            file.seek(PATCH_OFFSET)
            file.write(NOP)
        }
    }
}
