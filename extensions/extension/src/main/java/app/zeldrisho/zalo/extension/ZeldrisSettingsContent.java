package app.zeldrisho.zalo.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Content for the patch-created subclass of Zalo's BaseSettingView. */
public final class ZeldrisSettingsContent {
  private static final String PREFS = "zeldris_patch_settings";

  private ZeldrisSettingsContent() {}

  public static View build(LinearLayout parent) {
    Context context = parent.getContext();
    SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    boolean vi = isVietnamese(context);
    try {
      int layout = context.getResources().getIdentifier("setting_v2_view", "layout", context.getPackageName());
      int privacy = context.getResources().getIdentifier("itemPrivacy", "id", context.getPackageName());
      int messages = context.getResources().getIdentifier("itemMessage", "id", context.getPackageName());
      if (layout == 0 || privacy == 0 || messages == 0) return parent;
      LinearLayout temporary = new LinearLayout(context);
      LayoutInflater.from(context).inflate(layout, temporary, true);
      View discovery = temporary.findViewById(privacy);
      View timeline = temporary.findViewById(messages);
      if (discovery == null || timeline == null) return parent;
      configure(discovery, vi ? "Ẩn Khám phá" : "Hide Discovery", "hide_discovery", preferences, vi);
      configure(timeline, vi ? "Ẩn Nhật ký" : "Hide Timeline", "hide_timeline", preferences, vi);
      ((ViewGroup) discovery.getParent()).removeView(discovery);
      ((ViewGroup) timeline.getParent()).removeView(timeline);
      parent.addView(discovery);
      parent.addView(timeline);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // A host ABI mismatch must not crash the app.
    }
    return parent;
  }

  private static void configure(View item, String title, String key, SharedPreferences preferences, boolean vi)
      throws ReflectiveOperationException {
    Class<?> cls = item.getClass();
    cls.getMethod("setTitle", CharSequence.class).invoke(item, title);
    cls.getMethod("setLeadingItemVisibility", int.class).invoke(item, View.GONE);
    cls.getMethod("setShowChevronRight", boolean.class).invoke(item, false);
    cls.getMethod("setSwitch", boolean.class).invoke(item, preferences.getBoolean(key, true));
    CompoundButton.OnCheckedChangeListener listener = (button, checked) -> {
      preferences.edit().putBoolean(key, checked).apply();
      Toast.makeText(item.getContext(), vi ? "Khởi động lại Zalo để áp dụng" : "Restart Zalo to apply",
          Toast.LENGTH_SHORT).show();
    };
    cls.getMethod("setCheckedChangeListener", CompoundButton.OnCheckedChangeListener.class).invoke(item, listener);
  }

  public static void setTitle(Object screen) {
    try {
      Object actionBar = screen.getClass().getField("y1").get(screen);
      if (actionBar != null) actionBar.getClass().getMethod("setMiddleTitle", String.class)
          .invoke(actionBar, "Zeldris Patches");
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // Host action bar unavailable.
    }
  }

  @SuppressWarnings("deprecation")
  private static boolean isVietnamese(Context context) {
    return "vi".equals(context.getResources().getConfiguration().locale.getLanguage());
  }
}
