package app.zeldrisho.zalo.extension;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Optional black surfaces at Zalo's context-aware theme-color lookup; light mode is untouched. */
public final class AmoledTheme {
  private static final Set<String> SURFACES = new HashSet<>(Arrays.asList(
      "PrimaryBackgroundColor", "SecondaryBackgroundColor", "BottomNav", "TabBackgroundColor",
      "ChatBarBackgroundColor", "FooterBackgroundColor", "ProfilePrimaryBackgroundColor",
      "ProfileSecondaryBackgroundColor", "TimelinePrimaryBackgroundColor",
      "TimelineSecondaryBackgroundColor", "PopupBackgroundColor"));
  private static volatile Method darkMode;

  private AmoledTheme() {}

  /** Applied after Zalo installs its dark theme, so XML inflation sees the same black surfaces. */
  public static void applyDarkOverlay(Context context) {
    if (context == null) return;
    try {
      if (!context.getSharedPreferences("zeldris_patch_settings", Context.MODE_PRIVATE)
          .getBoolean("amoled_theme", false)) return;
      int style = context.getResources().getIdentifier("ZeldrisAmoledOverlay", "style", context.getPackageName());
      if (style != 0) context.getTheme().applyStyle(style, true);
    } catch (RuntimeException ignored) {
      // Preserve Zalo's native theme if the optional overlay is unavailable.
    }
  }

  public static boolean blackSurface(Context context, int attribute) {
    if (context == null) return false;
    try {
      if (!context.getSharedPreferences("zeldris_patch_settings", Context.MODE_PRIVATE)
          .getBoolean("amoled_theme", false)) return false;
      if (!"attr".equals(context.getResources().getResourceTypeName(attribute))
          || !SURFACES.contains(context.getResources().getResourceEntryName(attribute))) return false;
      Method method = darkMode;
      if (method == null) {
        method = Class.forName("ef1.c").getMethod("d", Context.class);
        darkMode = method;
      }
      return Boolean.TRUE.equals(method.invoke(null, context));
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      return false;
    }
  }
}
