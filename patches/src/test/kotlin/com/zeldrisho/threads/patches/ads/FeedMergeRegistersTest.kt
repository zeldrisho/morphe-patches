package com.zeldrisho.threads.patches.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedMergeRegistersTest {
    @Test fun listRegisterMath() {
        // registerCount - 9 params (incl. this) + index 5
        assertEquals(5, feedListRegister(9))
        assertEquals(11, feedListRegister(15))
    }

    @Test fun lowRegistersUsePlainMove() {
        assertEquals("move-object v0, v11", feedListLoadMove(11))
        assertEquals("move-object v11, v0", feedListStoreMove(11))
    }

    @Test fun highRegistersUseFrom16() {
        // Injected snippets only accept v0-v15; from16 covers the rest.
        // e.g. registerCount 25 -> listReg 21
        val reg = feedListRegister(25)
        assertEquals(21, reg)
        assertTrue(feedListLoadMove(reg).startsWith("move-object/from16"))
        assertTrue(feedListStoreMove(reg).startsWith("move-object/from16"))
    }

    @Test fun boundaryAtV15() {
        assertEquals("move-object v0, v15", feedListLoadMove(15))
        assertTrue(feedListLoadMove(16).startsWith("move-object/from16"))
    }
}
