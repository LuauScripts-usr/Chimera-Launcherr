# AGENTS.md

## Build
- `./gradlew :app:compileDebugJavaWithJavac` — fast Java-only validation.
- `./gradlew :app:compileDebugKotlin` — validates the Kotlin module (also compiles Java).
  - **Submodules required before assembling**: `app/src/main/cpp/preloader` (https://github.com/LuauScripts-usr/preloader-android) and `app/src/main/cpp/libHttpClient` (https://github.com/microsoft/libHttpClient( are git submodules; `CMakeLists.txt` add_subdirectories both. If uninitialized (`git submodule status` shows `-` prefix(, run `git submodule sync && git submodule update --init --recursive` first, then `./gradlew clean` (stale stub `libgxcore.so` can persist otherwise(.
  - Toolchain installed at `/opt/android-sdk` (`local.properties`; java at `/usr/lib/jvm/java-21-openjdk-amd64`(. `libgxcore.so` is a **prebuilt** drop-in at `app/src/main/jniLibs/` **arm64-v8a only**, ~3 MB; no armeabi-v7a gxcore by design. Verify in APK: `unzip -l app/build/outputs/apk/debug/app-debug.apk | grep gxcore`. Kit: `adb uninstall org.chimeramc.launcher` before installing a new APK (stale extracted native libs cached under app storage(.

## Controller architecture (org.chimeramc.launcher.launcher.controller + ui.views + ui.activities.ControllerActivity)
- `ControllerType` — enum mapping vendor/product IDs to Xbox (vendor 0x045E, any product), DS4 (0x054C / 0x05C4|0x09CC), DualSense (0x054C / 0x0CE6). `matches()` returns true for any product when `productIdA == -1`.
- `ControllerProfile` — serializable POJO: button remaps (Map<Integer,Integer> key→key), L/R stick dead zones, L/R sensitivity, vibration flag. Constants: MAX_SLOTS=5, DEFAULT_DEAD_ZONE=0.15f.
- `ControllerProfileManager` — persists up to 5 profiles per type + active slot index in SharedPreferences ("controller_profiles") as JSON (Gson). Methods: getProfiles/saveProfiles/getActiveSlot/setActiveSlot/getActiveProfile/addProfile/deleteProfile/duplicateProfile/renameProfile.
- `ControllerInputProcessor` — static bridge that applies the active profile to the real gameplay pipeline. `MinecraftActivity.kt` calls `detectAndLoad(this)` on create, `processKeyEvent(keyCode)` in `dispatchKeyEvent` (key remap), and `isWithinDeadZone(event)` in `dispatchGenericMotionEvent` (dead zone). `setActiveProfile(type, profile)` is called from ControllerActivity whenever the active profile changes.
- `ControllerIllustrationView` — custom `View` drawing flat top-down controller silhouettes + individually-highlightable button/stick regions per type; `setType`, `setRegionGlow`, `handleKeyEvent`, `handleMotionEvent`. Must keep both `(Context)` and `(Context, AttributeSet)` constructors so XML layout can inflate it.
- `ControllerActivity` — registers an `InputManager.InputDeviceListener`, auto-selects the illustration for a detected controller, otherwise shows the first (Xbox) with Manual "Next" (Xbox→DS4→DualSense→Xbox); provides profile chips + Create/Rename/Duplicate/Delete + editor dialog (name, L/R dead zone, L/R sensitivity, vibration).

## Editor note
- Android Activity/View APIs used: `android.hardware.input.InputManager`, `android.view.InputDevice` (getDeviceIds/getDevice/vendor/product/sources), `android.view.KeyEvent`/`MotionEvent` (getAxisValue(getAxisValue; MotionEvent has NO setAxisValue in this SDK). Custom `View` inflation requires `(Context)`/`(Context, AttributeSet)` ctors.
- Controller remaps/dead zones must be wired through `ControllerInputProcessor` (not just UI) to affect actual gameplay input; key remaps are applied to the `PreloaderInput.onKeyEvent` path in `MinecraftActivity.dispatchKeyEvent`.

## JNI/native packaging (org.levimc vs org.chimeramc)
- **Prebuilt** `libgxcore.so` and `libinbuiltmods.so` (in `app/src/main/jniLibs/arm64-v8a/`) still export symbols under the **upstream** `org.levimc.*` package names. Do NOT move their Java-bound classes too `org.chimeramc.*` or you get `UnsatisfiedLinkError: No implementation found.`:
  - `org.levimc.launcher.util.NativeBridgeHelper` (+ colocated `NativeImageGuard`) binds `Java_org_levimc_launcher_util_NativeBridgeHelper_*`
  - `org.levimc.launcher.core.mods.inbuilt.nativemod.*` (AutoSprint/Fps/Gyro/HotbarSlot/MoreButtons/PojavControls/Snaplook/Zoom + InbuiltModsNative) binds `Java_org_levimc_launcher_core_mods_inbuilt_nativemod_*`
- The preloader submodule source (`app/src/main/cpp/preloader`, not checked out in dev) is already branded `org.chimeramc.*` — so `ModManager`, `ExternalModBridge`, `PreloaderInput`, `MoreButtonsSvgBridge`, `MinecraftRuntimePreparer` natives stay in `org.chimeramc` packages.

- If the native libs ever get rebuilt against the chimeramc package, move these classes back and regenerate the binaries together.

## Personalization / theming (org.chimeramc.launcher.util.PersonalizationManager + org.chimeramc.launcher.launcher.ui.animation.DynamicAnim)
- `PersonalizationManager` (launcher/util) drives: accent color (`getAccentColor`/`setAccentColor`), animation speed, UI transparency, card rounding (`getCardRoundingPx` — note non-Px name), icon size, blur intensity, compact mode, dark-mode detection (`isDarkMode(Context)`), and the three accessibility toggles:
  - `isShowAnimations()` / `setShowAnimations(boolean)` — global animations on/off. Must gate ALL new motion: `DynamicAnim.disableAnimations()`/`enableAnimations()` only cover DynamicAnim land; custom ViewPropertyAnimators/ValueAnimators (e.g. hero-card pulse, progress tween in MainActivity) must check this flag themselves.
  - `isEnableGlowEffects()` / `setEnableGlowEffects(boolean)` — gates card elevation + gradient strokes (MainActivity.applyGlowEffects). "Reduced motion" flows through `isShowAnimations`. DO NOT hard-code new animations outside this gate.
- `DynamicAnim.applyPressScale(view)` / `applyPressScaleRecursively(root)` — press-scale + elevation micro-interaction; respects `animationsEnabled` (skips entirely when disabled). `setGlobalSpeedMultiplier(float)` scales spring durations.
- Gradient accent palette lives in `colors.xml` + `values-night/colors.xml` (`grad_hero_start/end`, `grad_launch_*`, `grad_versions_*`, `grad_mods_*`, `grad_content_*`, `grad_pulse_glow`) with per-section drawables `card_hero_gradient.xml`, `card_launch_gradient.xml`, `card_versions_gradient.xml`, `card_mods_gradient.xml`, `card_content_gradient.xml`, and `section_accent_bar.xml` (3dp vertical accent bars used as section headers). Keep dark/light variants in sync and respect `on_primary`/`on_surface` for text legibility on gradients.

## Game session lifecycle & playtime (org.chimeramc.launcher.launcher.core.minecraft.PlaytimeManager)
- `PlaytimeManager` (in the launcher.core.minecraft package, not `util`) is the per-instance playtime tracker: `init(context)`, `startSession(profileId)`, `heartbeat()`, `stopSession()`, `getTotalMs(profileId)`, `formatPlaytime(ms)`. Persistence is SharedPreferences `"playtime_tracker"` — `total_ms_<profileId>` accumulates, `active_profile`/`active_start_elapsed` hold the running session; `HEARTBEAT_INTERVAL_MS = 15s` (public). Hardware monitors use more deeply-nested SharedPreferences keys.
- On-device watchdog: `MinecraftActivity.kt` starts the session from intent extra `MinecraftLauncher.EXTRA_STORAGE_PROFILE_ID` (from `MinecraftLauncher.getStorageProfileId(version)`), sends a heartbeat via Handler every 15s, and calls `stopSession()` in `onDestroy`. `Application.kt` calls `PlaytimeManager.init()` to clean up an interrupted session (crash/kill).
- Playtime is shown on the main hero card (`last_played_time_stat`) and per-instance in `InstancesActivity` + `item_instance_card.xml`; hero stats animate via `DynamicAnim` counters, but the playtime value itself uses a plain `setText`.

## Low-latency networking (org.chimeramc.launcher.launcher.settings.LowLatencyNetworkManager)
- Settings → Basic "Reduce Network Latency" toggle (`FeatureSettings.isReduceNetworkLatencyEnabled()` / `setReduceNetworkLatencyEnabled`), label honestly — a launcher cannot promise "lowest ping". What it implements:
  - `createSocketFactory()` wraps the platform default and sets `TCP_NODELAY` on launcher-owned sockets (NewsRepository + GithubReleaseUpdater OkHttp builders apply it when the flag is on). Deliberately delegates to `SocketFactory.getDefault()` — calling the inherited abstract `super.createSocket(...)` does NOT compile.
  - `prefetchDnsOnBackground()` warms `PREFETCH_HOSTS` (raw.githubusercontent, api.github.com, api.curseforge.com, www.googleapis.com) on a single background executor; re-triggered when the toggle is turned on.
  - Game-session quiet zone: `setGameSessionActive(true)` (MinecraftActivity session start) makes `isGameSessionActive()` true; NewsRepository.refreshIfStale / GithubReleaseUpdater / LauncherNewsMessagingService then serve cached data instead of polling while a session runs.
- Java-only sockets: `features` flag lives in FeatureSettings (`isReduceNetworkLatencyEnabled`) — see `app/src/main/java/org/chimeramc/launcher/launcher/settings/FeatureSettings.java`.

## Runtime verification (no KVM/emulator in dev)
- No Android emulator/KVM here — verify via `./gradlew :app:compileDebugJavaWithJavac` (fast), `:app:compileDebugKotlin`, and a full `nohup ./gradlew :app:assembleDebug > /tmp/build_apk.log 2>&1 &` then grep for `BUILD SUCCESSFUL`/`FAILED`. To test on a device later (adb, ARM64 Android), install `app/build/outputs/apk/debug/app-debug.apk`, and:
  - Playtime: launch a version, wait ~20s, close; repeat — SharedPreferences `playtime_tracker` (total_ms_<profileId>) should accumulate ≈ real elapsed wall time (± a heartbeat interval).
  - Reduced-motion: enable Settings → Personalize → "show animations" OFF, confirm hero-card pulse stops (no translationZ/alpha oscillation) and press feedback is instant/no spring.
  - Glow: toggle "enable glow" OFF, confirm card elevation returns to 0 and gradient strokes disappear.
  - Latency: with the toggle ON, use `tcpdump`/`strace` or bpf to confirm TCP_NODELAY on launcher sockets, no DNS re-resolve during an active session, and that NewsRepository/GithubReleaseUpdater skip the network while a game is running.