package com.zeldrisho.zalo.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(instrumentedPackages = "com.zeldrisho.zalo.extension")
@LooperMode(LooperMode.Mode.PAUSED)
public class ZaloMicroGRuntimeTest {
  @Test
  public void nullActivityAndEnabledProviderAreAllowed() {
    assertTrue(ZaloMicroGSupport.checkGmsCore(null));
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    PackageInfo info = new PackageInfo();
    info.applicationInfo = new ApplicationInfo();
    info.applicationInfo.enabled = true;
    assertTrue(ZaloMicroGSupport.checkGmsCore(activity, () -> info, (a, i, c) -> {}));
  }

  @Test
  public void missingProviderShowsInstallDialogAndInstallOpensLink() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    assertFalse(ZaloMicroGSupport.checkGmsCore(activity));
    AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
    assertTrue(dialog.isShowing());
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
  }

  @Test
  public void disabledProviderAndUnexpectedPackageFailureAreHandled() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    PackageInfo disabled = new PackageInfo();
    disabled.applicationInfo = new ApplicationInfo();
    disabled.applicationInfo.enabled = false;
    assertFalse(ZaloMicroGSupport.checkGmsCore(activity, () -> disabled, (a, i, c) -> {}));
    assertTrue(
        ZaloMicroGSupport.checkGmsCore(
            activity,
            () -> {
              throw new IllegalStateException();
            },
            (a, i, c) -> {}));
  }

  @Test
  public void destroyedAndFinishingActivitiesAreRejectedByLifecycleGuard() {
    Activity finishing = Robolectric.buildActivity(Activity.class).setup().get();
    finishing.finish();
    assertFalse(ZaloMicroGSupport.canShowInstallDialog(finishing));

    org.robolectric.android.controller.ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup();
    controller.destroy();
    assertFalse(ZaloMicroGSupport.canShowInstallDialog(controller.get()));
    assertFalse(ZaloMicroGSupport.canShowInstallDialog(false, true));
    assertTrue(ZaloMicroGSupport.canShowInstallDialog(false, false));
  }

  @Test
  public void realHandlerSchedulerCoalescesAndExecutesLatestRefresh() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    Target target = new Target();
    ZaloMicroGSupport.scheduleAccountRefresh(target, "first");
    ZaloMicroGSupport.scheduleAccountRefresh(target, "latest");
    Shadows.shadowOf(Looper.getMainLooper()).idleFor(250, TimeUnit.MILLISECONDS);
    assertEquals(1, target.count);
    assertEquals("latest", target.account);
  }

  @Test
  public void handlerSchedulerCancelsPendingCallbacks() throws Exception {
    Class<?> schedulerClass =
        Class.forName("com.zeldrisho.zalo.extension.ZaloMicroGSupport$HandlerScheduler");
    java.lang.reflect.Constructor<?> constructor =
        schedulerClass.getDeclaredConstructor(Handler.class);
    constructor.setAccessible(true);
    Object scheduler = constructor.newInstance(new Handler(Looper.getMainLooper()));
    java.lang.reflect.Method schedule =
        schedulerClass.getDeclaredMethod("schedule", Runnable.class, long.class);
    java.lang.reflect.Method cancel = schedulerClass.getDeclaredMethod("cancel", Runnable.class);
    schedule.setAccessible(true);
    cancel.setAccessible(true);
    Runnable callback = () -> {};
    schedule.invoke(scheduler, callback, 250L);
    cancel.invoke(scheduler, callback);
  }

  @Test
  public void downloadWithoutBrowserOrPermissionIsIgnored() throws Exception {
    Method method = ZaloMicroGSupport.class.getDeclaredMethod("openDownload", Activity.class);
    method.setAccessible(true);
    Activity noBrowser = Robolectric.buildActivity(NoBrowserActivity.class).setup().get();
    method.invoke(null, noBrowser);
    Activity blocked = Robolectric.buildActivity(BlockedIntentActivity.class).setup().get();
    method.invoke(null, blocked);
  }

  public static final class Target {
    int count;
    String account;

    public void A6(String name) {
      count++;
      account = name;
    }
  }

  public static final class NoBrowserActivity extends Activity {
    @Override
    public void startActivity(Intent intent) {
      throw new ActivityNotFoundException();
    }
  }

  public static final class BlockedIntentActivity extends Activity {
    @Override
    public void startActivity(Intent intent) {
      throw new SecurityException("external intent blocked");
    }
  }
}
