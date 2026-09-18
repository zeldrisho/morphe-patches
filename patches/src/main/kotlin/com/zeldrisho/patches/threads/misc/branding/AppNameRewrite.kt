package com.zeldrisho.patches.threads.misc.branding

import com.zeldrisho.patches.shared.resources.rewriteAppLabel
import org.w3c.dom.Document

const val LAUNCHER_ACTIVITY = "com.instagram.barcelona.mainactivity.BarcelonaActivity"

/**
 * Pure, unit-testable core of [changeAppNamePatch]'s `execute {}` block.
 * Throws [IllegalStateException] when the manifest has no `<application>` so
 * failures are explicit instead of an NPE inside the patcher.
 */
fun applyAppName(document: Document, newName: String, launcherActivity: String = LAUNCHER_ACTIVITY) {
    rewriteAppLabel(document, newName, launcherActivity)
}
