package com.zeldrisho.zalo.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ZaloMicroGSupportTest {
  @Test
  public void enabledProviderIsAccepted() {
    assertTrue(ZaloMicroGSupport.isProviderEnabled(packageInfo(true)));
  }

  @Test
  public void disabledProviderIsRejected() {
    assertFalse(ZaloMicroGSupport.isProviderEnabled(packageInfo(false)));
  }

  @Test
  public void missingProviderIsRejected() {
    assertFalse(ZaloMicroGSupport.isProviderEnabled(null));
  }

  @Test
  public void providerWithoutApplicationInfoIsRejected() {
    assertFalse(ZaloMicroGSupport.isProviderEnabled(new PackageInfo()));
  }

  @Test
  public void invalidRefreshRequestsAreIgnoredWithoutTouchingTheMainLooper() {
    ZaloMicroGSupport.scheduleAccountRefresh(null, "account@example.com");
    ZaloMicroGSupport.scheduleAccountRefresh(new Object(), null);
    ZaloMicroGSupport.scheduleAccountRefresh(new Object(), "");
  }

  @Test
  public void delayedRefreshIgnoresMissingOrChangedTargetMethods() throws Exception {
    Constructor<?> constructor = refreshRequestConstructor();
    Runnable missingMethod =
        (Runnable) constructor.newInstance(new Object(), "account@example.com");
    missingMethod.run();

    Runnable nullTarget = (Runnable) constructor.newInstance(null, "account@example.com");
    nullTarget.run();
  }

  @Test
  public void delayedRefreshContainsTargetExceptions() throws Exception {
    Constructor<?> constructor = refreshRequestConstructor();
    Runnable failingTarget =
        (Runnable) constructor.newInstance(new FailingRefreshTarget(), "account@example.com");
    failingTarget.run();
  }

  @Test
  public void packageManagerFailuresFailOpen() {
    boolean result =
        ZaloMicroGSupport.checkGmsCore(
            new Activity(),
            () -> {
              throw new IllegalStateException("package manager unavailable");
            },
            (activity, install, cancel) -> {
              throw new AssertionError("prompt must not be shown");
            });
    assertTrue(result);
  }

  @Test
  public void missingProviderPromptsAndCancellationDoesNotInstall() {
    List<String> actions = new ArrayList<>();
    boolean result =
        ZaloMicroGSupport.checkGmsCore(
            new Activity(),
            () -> {
              throw new PackageManager.NameNotFoundException();
            },
            (activity, install, cancel) -> {
              actions.add("prompt");
              cancel.run();
            });
    assertFalse(result);
    assertEquals(List.of("prompt"), actions);
  }

  @Test
  public void finishingOrDestroyedActivitiesDoNotReceiveInstallDialogs() {
    assertFalse(ZaloMicroGSupport.canShowInstallDialog(true, false));
    assertFalse(ZaloMicroGSupport.canShowInstallDialog(false, true));
  }

  @Test
  public void unavailableBrowserIsContained() throws Exception {
    java.lang.reflect.Method method =
        ZaloMicroGSupport.class.getDeclaredMethod("openDownload", Activity.class);
    method.setAccessible(true);
    method.invoke(null, new NoBrowserActivity());
  }

  @Test
  public void repeatedSelectionCancelsTheOlderRefresh() {
    RecordingScheduler scheduler = new RecordingScheduler();
    Object target = new Object();
    ZaloMicroGSupport.scheduleAccountRefresh(target, "first@example.com", scheduler);
    ZaloMicroGSupport.scheduleAccountRefresh(target, "second@example.com", scheduler);
    assertEquals(2, scheduler.scheduled.size());
    assertEquals(1, scheduler.cancelled.size());
    assertNotSame(scheduler.cancelled.get(0), scheduler.scheduled.get(1));
  }

  private static Constructor<?> refreshRequestConstructor() throws Exception {
    Class<?> requestClass =
        Class.forName("com.zeldrisho.zalo.extension.ZaloMicroGSupport$RefreshRequest");
    Constructor<?> constructor = requestClass.getDeclaredConstructor(Object.class, String.class);
    constructor.setAccessible(true);
    return constructor;
  }

  private static final class FailingRefreshTarget {
    @SuppressWarnings("unused")
    public void A6(String accountName) {
      throw new IllegalStateException("simulated stale lifecycle target");
    }
  }

  private static final class NoBrowserActivity extends Activity {
    @Override
    public void startActivity(Intent intent) {
      throw new ActivityNotFoundException();
    }
  }

  private static final class RecordingScheduler implements ZaloMicroGSupport.RefreshScheduler {
    final List<Runnable> scheduled = new ArrayList<>();
    final List<Runnable> cancelled = new ArrayList<>();

    @Override
    public void schedule(Runnable request, long delayMillis) {
      scheduled.add(request);
      assertEquals(250L, delayMillis);
    }

    @Override
    public void cancel(Runnable request) {
      cancelled.add(request);
    }
  }

  private static PackageInfo packageInfo(boolean enabled) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.enabled = enabled;
    return packageInfo;
  }
}
