package app.zeldrisho.zalo.extension;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;

/** A persisted document URI used only for incoming-call ringtone selection. */
public final class CustomRingtone {
  static final String PREFS = "zeldris_patch_settings";
  static final String KEY = "custom_call_ringtone";

  private CustomRingtone() {}

  /** Retain the host ringtone if no readable persisted grant is available. */
  public static String resolve(Context context, String nativeRingtone) {
    if (context == null || nativeRingtone == null || nativeRingtone.isEmpty()) return nativeRingtone;
    try {
      String selected = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null);
      if (selected == null) return nativeRingtone;
      Uri uri = Uri.parse(selected);
      for (UriPermission permission : context.getContentResolver().getPersistedUriPermissions()) {
        if (permission.isReadPermission() && uri.equals(permission.getUri())) return selected;
      }
    } catch (RuntimeException ignored) {
      // A stale grant or preference must not prevent the host from ringing.
    }
    return nativeRingtone;
  }
}
