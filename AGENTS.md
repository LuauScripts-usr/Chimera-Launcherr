# AGENTS.md

## Build
- `./gradlew :app:compileDebugJavaWithJavac` — fast Java-only validation.
- `./gradlew :app:compileDebugKotlin` — validates the Kotlin module (also compiles Java).
- `./gradlew :app:assembleDebug` — full build; **fails on the native CMake step** (preloader/libHttpClient are git submodules that aren't checked out in dev), so it cannot be used to verify Java/Kotlin changes. Use the compile tasks above instead.
- Needs an Android SDK at `/opt/android-sdk`, configured via `local.properties` (`sdk.dir=/opt/android-sdk`). `local.properties` is gitignored.

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