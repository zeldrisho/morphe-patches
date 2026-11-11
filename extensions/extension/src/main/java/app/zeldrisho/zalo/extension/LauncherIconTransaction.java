package app.zeldrisho.zalo.extension;

/** Best-effort launcher transition; the old component is restored before rolling back the new one. */
final class LauncherIconTransaction {
  interface Components {
    int get(boolean destination);
    void set(boolean destination, int state);
  }

  private LauncherIconTransaction() {}

  static boolean select(Components components, int enabled, int disabled) {
    int previousDestination = components.get(true);
    int previousOld = components.get(false);
    components.set(true, enabled);
    try {
      components.set(false, disabled);
      return true;
    } catch (RuntimeException failure) {
      try {
        components.set(false, previousOld);
        components.set(true, previousDestination);
      } catch (RuntimeException ignored) {
        // If restoring the old component fails, keep the destination enabled.
      }
      return false;
    }
  }
}
