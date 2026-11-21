# Bob's Bunny: Carrot City — Icon / Splash Integration

Asset authority: user-approved icon/splash artwork, integrated on Draft PR #2. 1024px icon is a release candidate, **not** an approved final GLB/rig or store certification.

- App icon master: `docs/branding/bobs_carrot_city/icon_master.png` (1024px, RGB).
- Android adaptive foreground: `drawable-nodpi/bobs_launcher_foreground.png` (432px RGBA).
- Android adaptive background: `drawable-nodpi/bobs_launcher_background.png` (432px RGB).
- Android pre-Compose window background: `drawable-nodpi/bobs_carrot_city_splash.webp` (1920×1080). This creates a branded window background during Activity startup; it is **not** a timed in-game splash overlay or proof of device screenshot quality.
- Monochrome/tinted existing launcher icon intentionally retained.
- Existing 2D fallback, W1-1 game engine and progression unchanged.

Required: `./gradlew testDebugUnitTest assembleDebug`, Android instrumentation with real GLES3 first-frame evidence, icon mask audit, launch/resume on physical device, performance and API26 check, app ID/Firebase migration, signing and Play review. No release publication authorized.
