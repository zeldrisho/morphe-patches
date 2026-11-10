package app.zeldrisho.patches.zalo.misc

import app.morphe.patcher.patch.resourcePatch
import app.zeldrisho.patches.shared.Constants.COMPATIBILITY_ZALO_261002
import org.w3c.dom.Element

/** Explicit dark surface tokens only; never blanket-replace all grey/black colors. */
internal val amoledSurfaceColors = setOf(
    "Dark_PrimaryBackgroundColor",
    "Dark_SecondaryBackgroundColor",
    "Dark_BottomNav",
    "Dark_TabBackgroundColor",
    "Dark_ChatBarBackgroundColor",
    "Dark_FooterBackgroundColor",
    "Dark_ProfilePrimaryBackgroundColor",
    "Dark_ProfileSecondaryBackgroundColor",
    "Dark_TimelinePrimaryBackgroundColor",
    "Dark_TimelineSecondaryBackgroundColor",
    "Dark_PopupBackgroundColor",
)

@Suppress("unused")
val zaloAmoledThemePatch = resourcePatch(
    name = "AMOLED dark theme",
    description = "Makes selected native dark-mode surfaces pure black. Enable Zalo's dark theme separately.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_ZALO_261002)
    execute {
        document("res/values/colors.xml").use { document ->
            val colors = document.getElementsByTagName("color")
            val matched = mutableSetOf<String>()
            for (i in 0 until colors.length) {
                val color = colors.item(i) as Element
                val name = color.getAttribute("name")
                if (name in amoledSurfaceColors) {
                    check(matched.add(name)) { "Duplicate AMOLED surface: $name" }
                    color.textContent = "#FF000000"
                }
            }
            check(matched == amoledSurfaceColors) {
                "AMOLED dark surface tokens changed: ${amoledSurfaceColors - matched}"
            }
        }
    }
}
