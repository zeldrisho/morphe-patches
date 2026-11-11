package app.zeldrisho.zalo.extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/** In-memory snapshot of hashed recall keys; preference changes invalidate the snapshot. */
final class RecallKeyCache {
  private Set<String> keys;

  synchronized Set<String> get(Supplier<Set<String>> load) {
    if (keys == null) keys = Collections.unmodifiableSet(new HashSet<>(load.get()));
    return keys;
  }

  synchronized void invalidate() {
    keys = null;
  }
}
