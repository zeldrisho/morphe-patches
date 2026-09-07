package com.zeldrisho.threads.patches.shared

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration guard on the GENERATED patches-list.json (see AGENTS.md: never
 * hand-edit — run `./gradlew generatePatchesList`). Fails when the bundle
 * shape drifts: wrong package group, missing patch, or unpinned version.
 */
class PatchesListShapeTest {
    private fun listJson(): String {
        val candidates = listOf(
            File("../patches-list.json"), // working dir = patches/
            File("patches-list.json"), // working dir = repo root
        )
        return candidates.firstOrNull { it.isFile }?.readText()
            ?: error("patches-list.json not found; run ./gradlew generatePatchesList")
    }

    @Test fun threadsBundleShape() {
        val json = listJson()
        for (name in listOf("Hide ads", "Remove AD_ID permission", "Change app name", "Change package name")) {
            assertTrue(json.contains("\"name\": \"$name\""), "missing patch: $name")
        }
        assertTrue(json.contains("com.instagram.barcelona"), "missing package group")
        assertTrue(json.contains("434.0.0.41.74"), "Threads target version must stay pinned")
    }

    @Test fun patchCountMatchesSources() {
        // 4 user-visible Threads patches; template example patches are unnamed/internal.
        val json = listJson()
        val names = Regex("\"name\": \"(.*?)\"").findAll(json).map { it.groupValues[1] }.toList()
        val threadsPatches = names.filter {
            it in setOf("Hide ads", "Remove AD_ID permission", "Change app name", "Change package name")
        }
        assertEquals(4, threadsPatches.size, "expected 4 Threads patches, found: $threadsPatches")
    }
}
