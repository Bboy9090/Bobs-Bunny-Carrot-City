# Bob's Bunny — Wave 4 3D Diorama integration qualification

**Branch:** `feat/bobs-bunny-3d-diorama`  
**PR:** [#2](https://github.com/Bboy9090/Bobs-Bunny-Carrot-City/pull/2) (Draft; targets `feat/bobs-bunny-integration`)  
**Code commits:** `3440512`, `b10a851`  
**Status:** Functional vertical-slice qualification, NOT art or store-release approval.

## Changes in this wave

- **Renderer safety:** shader compile/link checks with descriptive failures, first-drawn-frame signal, loading overlay, OpenGL ES 3.0 capability check and automatic 2D fallback on renderer initialization failure.
- **UI:** landscape-focused phone activity, more compact two-line speech bubble to the right of the character, narrow button text fitted without overflow, touch-control accessibility actions.
- **Navigation regression:** scrollable world-filter selector has stable semantics; instrumented test drives W1-1 3D → 2D → 3D without replacing its game engine or stage state.

## Captured evidence

![Landscape Home Hub with W1-1 start button](home-hub-landscape.png)

![W1-1 active 3D Diorama showing Bob, skyline and buttons](w1-1-3d-diorama-live.png)

The second image confirms that the 3D scene rendered after initialization and Bob is no longer obscured by the previous full-width dialogue banner. The screenshots show a **procedural low-poly proof of concept**, not final approved production artwork.

## Test and build record

| Gate | Result |
| --- | --- |
| Android Gradle `testDebugUnitTest` | **11/11 passing** (JUnit XML) |
| Android Gradle `connectedDebugAndroidTest` | **7/7 passing**, no failures, errors or skips (JUnit XML) |
| Android Gradle `assembleDebug` | **PASS** |
| Tested emulator | Android API 37, x86_64, ANGLE/SwiftShader OpenGL ES 3 |
| Debug APK size | ~16 MB |
| Debug APK SHA-256 | `35180a41956345f5a2fd30d94286f4c5ae3f402c826cff9eb728bc321434a963` |
| GitHub Actions Android CI | [Wave 4 run #44013123190](https://github.com/Bboy9090/Bobs-Bunny-Carrot-City/actions/runs/44013123190): **success** |

One initial emulator run failed 2/6 UI tests because it booted in portrait and the narrow world filter could not expose the expected world navigation. The updated landscape configuration and scrolling test selector were subsequently verified: the final **7/7 instrumentation pass** supersedes that earlier failure. This prevents misreporting the initial result as green.

## Remaining release blockers

1. **Art sign-off:** procedural Bob and the Carrot City diorama are technical placeholders awaiting project-owner visual review. Final rigged model, mesh fidelity, textured materials and environment art remain to be produced.
2. **Real-device QA:** no Adreno/Mali GPU, physical touch latency, thermal and sustained FPS measurements. API 26 runtime qualification remains missing.
3. **Full gameplay:** W1-1 has a tested 3D/2D toggle but still requires a player-controlled start-to-finish playthrough, including checkpoints, carrots, foes and stage-completion rewards.
4. **Concurrency:** `CarrotCity3DRenderer` reads mutable `CartoonStageEngine` fields directly from the GL thread. A bounded immutable render-snapshot layer is the next important engineering task.
5. **Android identity:** development app ID `com.example.myapplication` and matching `google-services.json` must be migrated together before a Play release. Do not rename only one side.
6. **Cross-device layout:** fixed landscape orientation is appropriate for the current phone platformer; confirm resize and orientation behavior on foldables and tablets as their Android policies may differ.

**Decision:** Continue the Wave 4 art-quality vertical slice. Retain PR #2 as Draft. Do not merge or publish until visual and physical-device gates pass.
