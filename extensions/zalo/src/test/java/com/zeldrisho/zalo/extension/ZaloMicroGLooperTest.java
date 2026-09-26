package com.zeldrisho.zalo.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Looper;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class ZaloMicroGLooperTest {
  @Test
  public void missingProviderShowsPromptAndCancelClosesIt() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();

    assertFalse(
        ZaloMicroGSupport.checkGmsCore(
            activity,
            () -> {
              throw new PackageManager.NameNotFoundException();
            },
            (current, install, cancel) ->
                new AlertDialog.Builder(current)
                    .setTitle("MicroG required")
                    .setNegativeButton("Cancel", (dialog, which) -> cancel.run())
                    .setPositiveButton("Install", (dialog, which) -> install.run())
                    .show()));
    AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
    assertTrue(dialog.isShowing());
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
    Shadows.shadowOf(Looper.getMainLooper()).idle();
    assertFalse(dialog.isShowing());
  }

  @Test
  public void finishedActivityDoesNotShowProviderPrompt() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.finish();

    assertFalse(ZaloMicroGSupport.canShowInstallDialog(activity));
  }

  @Test
  public void mainLooperRunsScheduledAccountRefreshAfterDelay() {
    RefreshTarget target = new RefreshTarget();

    ZaloMicroGSupport.scheduleAccountRefresh(target, "account@example.com");
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(249, TimeUnit.MILLISECONDS);
    assertEquals(0, target.refreshCount);

    Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.MILLISECONDS);
    assertEquals(1, target.refreshCount);
  }

  public static final class RefreshTarget {
    int refreshCount;

    public void A6(String accountName) {
      refreshCount++;
    }
  }
}
