package app.zeldrisho.zalo.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/** Content for the patch-created subclass of Zalo's BaseSettingView. */
public final class ZeldrisSettingsContent {
  private static final String PREFS = "zeldris_patch_settings";

  private ZeldrisSettingsContent() {}

  /** Replaced at patch time only when the corresponding feature is installed. */
  public static boolean hasNavigationSettings() { return false; }
  public static boolean hasStrangerNotificationSettings() { return false; }
  public static boolean hasBackupIntervalSettings() { return false; }

  public static View build(LinearLayout parent) {
    Context context = parent.getContext();
    SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    boolean vi = isVietnamese(context);
    try {
      int layout = context.getResources().getIdentifier("setting_v2_view", "layout", context.getPackageName());
      int privacy = context.getResources().getIdentifier("itemPrivacy", "id", context.getPackageName());
      int messages = context.getResources().getIdentifier("itemMessage", "id", context.getPackageName());
      int notifications = context.getResources().getIdentifier("itemNotification", "id", context.getPackageName());
      if (layout == 0 || privacy == 0 || messages == 0 || notifications == 0) return parent;
      LinearLayout temporary = new LinearLayout(context);
      LayoutInflater.from(context).inflate(layout, temporary, true);
      if (hasNavigationSettings()) {
        View discovery = temporary.findViewById(privacy);
        View timeline = temporary.findViewById(messages);
        if (discovery == null || timeline == null) return parent;
        configure(discovery, vi ? "Ẩn Khám phá" : "Hide Discovery", "hide_discovery",
            "zds_ic_more_grid_line_24", true, preferences, vi);
        configure(timeline, vi ? "Ẩn Nhật ký" : "Hide Timeline", "hide_timeline",
            "zds_ic_new_feed_line_24", true, preferences, vi);
        ((ViewGroup) discovery.getParent()).removeView(discovery);
        ((ViewGroup) timeline.getParent()).removeView(timeline);
        parent.addView(discovery);
        parent.addView(timeline);
      }
      if (hasStrangerNotificationSettings()) {
        View stranger = temporary.findViewById(notifications);
        if (stranger == null) return parent;
        configure(stranger, vi ? "Cho phép thông báo người lạ" : "Allow stranger notifications",
            "stranger_notifications", "zds_ic_stranger_line_24", true, preferences, vi);
        ((ViewGroup) stranger.getParent()).removeView(stranger);
        parent.addView(stranger);
      }
      if (hasBackupIntervalSettings()) {
        int backupId = context.getResources().getIdentifier("itemBackupAndRestore", "id", context.getPackageName());
        View backup = temporary.findViewById(backupId);
        if (backup == null) return parent;
        Class<?> cls = backup.getClass();
        cls.getMethod("setTitle", CharSequence.class).invoke(backup, vi ? "Khoảng thời gian sao lưu" : "Backup interval");
        configureIcon(backup, "zds_ic_backup_message_line_24");
        cls.getMethod("setShowChevronRight", boolean.class).invoke(backup, true);
        backup.setOnClickListener(view -> ZeldrisSettings.openBackupSettings());
        ((ViewGroup) backup.getParent()).removeView(backup);
        parent.addView(backup);
      }
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // A host ABI mismatch must not crash the app.
    }
    return parent;
  }

  private static void configure(View item, String title, String key, String iconName, boolean defaultValue,
      SharedPreferences preferences, boolean vi)
      throws ReflectiveOperationException {
    Class<?> cls = item.getClass();
    cls.getMethod("setTitle", CharSequence.class).invoke(item, title);
    configureIcon(item, iconName);
    cls.getMethod("setShowChevronRight", boolean.class).invoke(item, false);
    cls.getMethod("setSwitch", boolean.class).invoke(item, preferences.getBoolean(key, defaultValue));
    CompoundButton.OnCheckedChangeListener listener = (button, checked) -> {
      preferences.edit().putBoolean(key, checked).apply();
      Toast.makeText(item.getContext(), vi ? "Khởi động lại Zalo để áp dụng" : "Restart Zalo to apply",
          Toast.LENGTH_SHORT).show();
    };
    cls.getMethod("setCheckedChangeListener", CompoundButton.OnCheckedChangeListener.class).invoke(item, listener);
  }

  private static void configureIcon(View item, String iconName) throws ReflectiveOperationException {
    Context context = item.getContext();
    Class<?> cls = item.getClass();
    int iconId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    if (iconId != 0) {
      ImageView icon = new ImageView(context);
      int size = (int) (24 * context.getResources().getDisplayMetrics().density);
      icon.setLayoutParams(new FrameLayout.LayoutParams(size, size));
      icon.setImageResource(iconId);
      icon.setColorFilter(0xFF0068FF);
      cls.getMethod("m", View.class).invoke(item, icon);
      cls.getMethod("setLeadingItemVisibility", int.class).invoke(item, View.VISIBLE);
    } else {
      cls.getMethod("setLeadingItemVisibility", int.class).invoke(item, View.GONE);
    }
  }

  /** Builds a native single-choice screen; zero preserves the account's native interval. */
  public static View buildBackupInterval(LinearLayout parent) {
    Context context = parent.getContext();
    SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    boolean vi = isVietnamese(context);
    int[] hours = {0, 1, 3, 6, 12};
    String[] names = {"itemPrivacy", "itemMessage", "itemNotification", "itemCall", "itemContact"};
    String[] titles = vi ? new String[] {"Mặc định", "1 giờ", "3 giờ", "6 giờ", "12 giờ"}
        : new String[] {"Native", "1 hour", "3 hours", "6 hours", "12 hours"};
    try {
      int layout = context.getResources().getIdentifier("setting_v2_view", "layout", context.getPackageName());
      if (layout == 0) return parent;
      LinearLayout temporary = new LinearLayout(context);
      LayoutInflater.from(context).inflate(layout, temporary, true);
      View[] rows = new View[hours.length];
      int selected = preferences.getInt("backup_interval_hours", 0);
      if (selected != 1 && selected != 3 && selected != 6 && selected != 12) selected = 0;
      for (int i = 0; i < rows.length; i++) {
        int id = context.getResources().getIdentifier(names[i], "id", context.getPackageName());
        View row = temporary.findViewById(id);
        if (row == null) return parent;
        Class<?> cls = row.getClass();
        cls.getMethod("setTitle", CharSequence.class).invoke(row, titles[i]);
        cls.getMethod("setLeadingItemVisibility", int.class).invoke(row, View.GONE);
        cls.getMethod("setShowChevronRight", boolean.class).invoke(row, false);
        cls.getMethod("setTick", boolean.class).invoke(row, selected == hours[i]);
        rows[i] = row;
      }
      for (int i = 0; i < rows.length; i++) {
        final int index = i;
        View row = rows[i];
        row.setOnClickListener(view -> {
          preferences.edit().putInt("backup_interval_hours", hours[index]).apply();
          for (int j = 0; j < rows.length; j++) {
            try {
              rows[j].getClass().getMethod("setTick", boolean.class).invoke(rows[j], j == index);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
              // Keep the saved selection if the host cannot redraw a tick.
            }
          }
        });
        ((ViewGroup) row.getParent()).removeView(row);
        parent.addView(row);
      }
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // Keep the host's native backup interval if the screen cannot be built.
    }
    return parent;
  }

  public static void setTitle(Object screen) {
    setNativeTitle(screen, "Zeldris Patches");
  }

  public static void setBackupTitle(Object screen) {
    try {
      Context context = (Context) screen.getClass().getMethod("M5").invoke(screen);
      setNativeTitle(screen, context != null && isVietnamese(context)
          ? "Khoảng thời gian sao lưu" : "Backup interval");
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      setNativeTitle(screen, "Backup interval");
    }
  }

  private static void setNativeTitle(Object screen, String title) {
    try {
      Object actionBar = screen.getClass().getField("y1").get(screen);
      if (actionBar != null) actionBar.getClass().getMethod("setMiddleTitle", String.class).invoke(actionBar, title);
    } catch (ReflectiveOperationException | RuntimeException ignored) {
      // Host action bar unavailable.
    }
  }

  @SuppressWarnings("deprecation")
  private static boolean isVietnamese(Context context) {
    return "vi".equals(context.getResources().getConfiguration().locale.getLanguage());
  }
}
