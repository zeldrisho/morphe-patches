# Source provenance and update policy

Borrowed or adapted code must carry attribution next to the implementation and
be recorded here with its upstream URL, revision, local deviations, and update
policy before it is changed.

## Current inventory

- `patches/src/main/kotlin/com/zeldrisho/patches/shared/bytecode/MethodExtensions.kt`
  adapts the method-body cleanup pattern from doom-patches, with patterns also
  derived from ReVanced/BiliRoamingX. It is maintained as a local, dependency-free
  implementation because it relies on Morphe's mutable-method API. Re-check the
  upstream implementations and the resolved dexlib2 layout before changing it;
  update attribution and revision details when a specific upstream revision is
  adopted.
- The Android extension modules contain project-owned runtime code. They do not
  vendor third-party executable source.

Executable filtering rules remain fixed in the repository and are updated only
through reviewed source changes and a bundle release; they are never fetched at
runtime.
