package app.zeldrisho.zalo.extension;

/** Selection policy for the native account-specific backup interval. */
public final class BackupIntervalPolicy {
  private BackupIntervalPolicy() {}

  public static long select(long nativeInterval, int hours) {
    switch (hours) {
      case 1:
      case 3:
      case 6:
      case 12:
        return hours * 3_600_000L;
      default:
        return nativeInterval;
    }
  }
}
