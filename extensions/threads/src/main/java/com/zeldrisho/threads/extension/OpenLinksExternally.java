package com.zeldrisho.threads.extension;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** Opens valid web URLs through Android's normal external activity resolution. */
public final class OpenLinksExternally {
  /** Prevents instantiation of this static link handler. */
  private OpenLinksExternally() {}

  /** Returns true only when an external activity was successfully launched. */
  public static boolean open(Context context, String rawUrl) {
    if (context == null || rawUrl == null) return false;
    try {
      java.net.URI validated = new java.net.URI(rawUrl.trim());
      String validatedScheme = validated.getScheme();
      if (validatedScheme == null
          || !(validatedScheme.equalsIgnoreCase("http")
              || validatedScheme.equalsIgnoreCase("https"))
          || validated.getHost() == null
          || validated.getHost().isEmpty()) {
        return false;
      }
      Uri uri = Uri.parse(rawUrl.trim());
      String scheme = uri.getScheme();
      if (scheme == null
          || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
          || uri.getHost() == null
          || uri.getHost().isEmpty()) {
        return false;
      }
      Intent intent = new Intent(Intent.ACTION_VIEW, uri);
      android.content.ComponentName resolved = intent.resolveActivity(context.getPackageManager());
      if (resolved != null && context.getPackageName().equals(resolved.getPackageName()))
        return false;
      if (!(context instanceof android.app.Activity)) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      }
      context.startActivity(intent);
      return true;
    } catch (java.net.URISyntaxException ignored) {
      return false;
    } catch (ActivityNotFoundException | SecurityException ignored) {
      return false;
    } catch (RuntimeException ignored) {
      // Malformed URLs or host-context failures should retain Threads' own handler.
      return false;
    }
  }
}
