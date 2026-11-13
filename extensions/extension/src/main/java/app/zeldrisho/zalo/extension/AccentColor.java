package app.zeldrisho.zalo.extension;

import android.content.Context;
import android.content.res.Resources;

/** Optional overrides for a small, explicitly identified set of themed accent attributes. */
public final class AccentColor {
  static final String PREFS = "zeldris_patch_settings";
  static final String KEY = "accent_color";

  private AccentColor() {}

  /** Zero means use Zalo's unmodified color; selected colors are always opaque. */
  public static int resolve(Context context, int attribute) {
    if (context == null) return 0;
    try {
      Resources resources = context.getResources();
      if (!"attr".equals(resources.getResourceTypeName(attribute))) return 0;
      String name = resources.getResourceEntryName(attribute);
      if (!"AppPrimaryColor".equals(name) && !"AppAccentColor".equals(name)
          && !"AppBrandingColor".equals(name)) return 0;
      int color = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, 0);
      return color == 0 ? 0 : color | 0xff000000;
    } catch (RuntimeException ignored) {
      return 0;
    }
  }
}
