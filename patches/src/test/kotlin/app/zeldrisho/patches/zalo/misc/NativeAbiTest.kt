package app.zeldrisho.patches.zalo.misc

import app.zeldrisho.patches.zalo.native.nonArm64NativeEntries
import kotlin.test.Test
import kotlin.test.assertEquals

class NativeAbiTest {
    @Test fun removesOnlyNonArm64LibraryEntries() {
        val entries = listOf("lib/arm64-v8a/libnative_utils.so", "lib/armeabi-v7a/libnative_utils.so",
            "lib/x86/libtest.so", "lib/x86_64/libtest.so", "assets/file")
        assertEquals(entries.subList(1, 4), nonArm64NativeEntries(entries))
    }
}
