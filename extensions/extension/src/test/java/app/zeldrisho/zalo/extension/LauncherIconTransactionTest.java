package app.zeldrisho.zalo.extension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LauncherIconTransactionTest {
  private static final int DEFAULT = 0, ENABLED = 1, DISABLED = 2;

  private static final class FakeComponents implements LauncherIconTransaction.Components {
    int destination = DEFAULT, old = ENABLED, calls;
    int failAt = -1, failAgainAt = -1;
    final List<String> writes = new ArrayList<>();

    @Override public int get(boolean destination) { return destination ? this.destination : old; }
    @Override public void set(boolean destination, int state) {
      writes.add((destination ? "destination:" : "old:") + state);
      calls++;
      if (calls == failAt || calls == failAgainAt) throw new IllegalStateException();
      if (destination) this.destination = state; else old = state;
    }
  }

  @Test public void successfulTransitionEnablesFirst() {
    FakeComponents fake = new FakeComponents();
    assertTrue(LauncherIconTransaction.select(fake, ENABLED, DISABLED));
    assertEquals(List.of("destination:1", "old:2"), fake.writes);
    assertEquals(ENABLED, fake.destination);
    assertEquals(DISABLED, fake.old);
  }

  @Test public void failureDisablingOldRestoresBothOriginalStates() {
    FakeComponents fake = new FakeComponents();
    fake.failAt = 2;
    assertFalse(LauncherIconTransaction.select(fake, ENABLED, DISABLED));
    assertEquals(List.of("destination:1", "old:2", "old:1", "destination:0"), fake.writes);
    assertEquals(DEFAULT, fake.destination);
    assertEquals(ENABLED, fake.old);
  }

  @Test public void failedOldRestoreKeepsDestinationEnabled() {
    FakeComponents fake = new FakeComponents();
    fake.failAt = 2;
    fake.failAgainAt = 3;
    assertFalse(LauncherIconTransaction.select(fake, ENABLED, DISABLED));
    assertEquals(ENABLED, fake.destination);
    assertEquals(List.of("destination:1", "old:2", "old:1"), fake.writes);
  }

  @Test public void failureEnablingDestinationDoesNotDisableOld() {
    FakeComponents fake = new FakeComponents();
    fake.failAt = 1;
    assertThrows(IllegalStateException.class, () -> LauncherIconTransaction.select(fake, ENABLED, DISABLED));
    assertEquals(List.of("destination:1"), fake.writes);
    assertEquals(ENABLED, fake.old);
  }
}
