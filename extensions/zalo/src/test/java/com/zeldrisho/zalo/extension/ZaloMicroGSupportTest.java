package com.zeldrisho.zalo.extension;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.ApplicationInfo;
import java.lang.reflect.Constructor;
import android.content.pm.PackageInfo;
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
    Runnable missingMethod = (Runnable) constructor.newInstance(new Object(), "account@example.com");
    missingMethod.run();

    Runnable nullTarget = (Runnable) constructor.newInstance(null, "account@example.com");
    nullTarget.run();
  }

  @Test
  public void delayedRefreshContainsTargetExceptions() throws Exception {
    Constructor<?> constructor = refreshRequestConstructor();
    Runnable failingTarget = (Runnable) constructor.newInstance(new FailingRefreshTarget(), "account@example.com");
    failingTarget.run();
  }


  private static Constructor<?> refreshRequestConstructor() throws Exception {
    Class<?> requestClass = Class.forName(
        "com.zeldrisho.zalo.extension.ZaloMicroGSupport$RefreshRequest");
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

  private static PackageInfo packageInfo(boolean enabled) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.enabled = enabled;
    return packageInfo;
  }
}
