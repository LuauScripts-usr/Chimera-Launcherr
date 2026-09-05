# F-Droid Build Instructions for Chimera Launcher

## Prerequisites
- Android SDK 35+
- NDK r28
- Java 21

## Building for F-Droid

This build is fully de-Googled and contains no proprietary Google services.

### Removed Google Dependencies:
- Firebase Crashlytics
- Google Play Services
- Google Services Plugin

### Build Commands:
```bash
./gradlew clean assembleRelease
```

### Verification:
- No Google Play Services dependencies
- No Firebase libraries
- No proprietary tracking
- Fully open-source compatible

## Microsoft OAuth Setup (Optional)
To enable Microsoft login, users must:
1. Register an Azure AD application at https://portal.azure.com
2. Configure redirect URI: `msauth://org.chimeramc.launcher`
3. Add client ID to app settings

## Java Edition Support
The launcher includes experimental Java Edition support via the JavaEditionModManager class. This requires:
- External Java runtime (not bundled)
- User-provided Java Edition files

## Mod Sources
- CurseForge API (user must provide API key)
- Modrinth API (open)
- Local mod imports

## License
Open-source compatible - ready for F-Droid submission
