# Bob's Bunny: Carrot City — Android release identity and production preflight

This record belongs to Draft PR #2. This is **not** app-store approval.

## Verified cleanup, November 19, 2026

The original AI baking template was not reachable from `MainActivity`. Code search found `BakingScreen` referenced only by itself, `BakingViewModel` only by that screen, and `UiState` only by those two files. This cleanup removes their orphan Kotlin files, the unused Firebase AI SDK, Google Services Gradle plugin and the sample `google-services.json`. Historical versions remain recoverable in Git.

The actual game is local/offline for its documented game loop; this cleanup does not implement or promise future cloud/social functionality. If real multiplayer, live operations or user accounts require Firebase, that integration must be separately designed, approved and registered under the final app identity.

## Identity and signing are still blocked

- The Android `namespace` and `applicationId` currently use `com.example.myapplication`. The development build may keep it to preserve CI and local saves; **Play publication is prohibited** with this placeholder.
- Choose and confirm one permanent Play package name with the owner **before** changing `applicationId`; an uploaded Play app cannot arbitrarily change package identity.
- For an eventual production release, register the chosen package under the correct Google Play developer account; handle upload keys and Play App Signing using GitHub secrets or protected runners (never commit keystore files, passwords or production credentials).
- Run `python3 scripts/check_release_identity.py --profile debug` on every debug CI and `python3 scripts/check_release_identity.py --profile release` before attempting release signing. The latter intentionally rejects the current placeholder ID and missing release signer.
- Confirm Play target SDK policy, privacy/data-safety declarations, actual dependencies, AAB content, icon/splash assets, accessible UI and Android API26+ compatibility.
- Qualify API26 and real Adreno/Mali hardware, background/resume, sustained performance and full stage completion.
- Art owner approval in issue #3 and physical-device qualification in issue #4 are separate hard gates. Neither is replaced by this script.

**Decision:** keep PR #2 Draft; retain debug CI success while transparently recording production-readiness failure. No store account, Firebase account, or signing credentials were created by this change.
