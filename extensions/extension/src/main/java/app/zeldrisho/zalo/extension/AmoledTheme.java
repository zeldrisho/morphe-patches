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
