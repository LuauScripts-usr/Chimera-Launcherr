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