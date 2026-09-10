# Lessons learned

Short, reusable rules from incidents in this repository. Procedures belong in
[development](development.md), [patch development](patch-development.md), and
[QA](qa-checklist.md).

## Patching

| Rule | Why |
| --- | --- |
| Prove a client-side gate before bypassing a server response or telemetry signal. | Otherwise the proposed patch has no effect or suppresses harmless reporting. |
| Preserve initialization and error handling; patch the narrow predicate or callsite. | Shared helpers and lifecycle code have broad, hard-to-test consequences. |
| Filter returned collections rather than mutating unknown list implementations. | Immutable or shared lists can reject in-place edits or corrupt later consumers. |
| Guard version, ABI, and method-shape assumptions in native and bytecode patches. | A patch that matches the wrong artifact can crash or silently do nothing. |
| Treat a successful build as necessary, not sufficient. | Runtime behavior can differ after signing, installation, optimization, or data restoration. |

## Analysis and diagnostics

| Rule | Why |
| --- | --- |
| Establish a stock control before interpreting a patched run. | It separates app/device/toolchain failures from the change under test. |
| Use staged isolation: loader, entrypoint, helper, then predicate. | Broad stubs can hide the failure while also removing required setup. |
| Record exact artifact hashes, version, ABI, offsets, bytes, and raw logs. | Findings are otherwise not reproducible or portable to another build. |
| Prefer fail-loud diagnostic probes over broad hooks. | Broad instrumentation can perturb timing, loading, or register state. |
| Do not retain a diagnostic mutation as a production patch. | Stubs and NOPs commonly remove registration, cleanup, or required state. |
| When isolating native startup failures, preserve setup and neutralize only the failing dispatch. | Bypassing an entire initializer can remove required TLS/JNI state and create misleading secondary crashes. |

## Compatibility and QA

| Rule | Why |
| --- | --- |
| Keep package identity and signing assumptions explicit. | Renaming or re-signing can break authentication, app links, or native checks. |
| Test cold start, foreground/background transitions, and the main user flow. | Startup success alone does not prove lifecycle or feature compatibility. |
| Verify backup and restore with the app's supported mechanism. | External files are not necessarily a complete application-data backup. |
| Keep risky patches disabled until device QA proves the default path. | A patch should fail safely and remain easy to remove. |
