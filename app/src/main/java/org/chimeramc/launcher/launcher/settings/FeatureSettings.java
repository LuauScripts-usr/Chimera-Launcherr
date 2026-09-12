package org.chimeramc.launcher.settings;

import android.content.Context;

public class FeatureSettings {
    private static volatile FeatureSettings INSTANCE;
    private static Context appContext;
    private boolean versionIsolationEnabled = false;
    private boolean launcherManagedMcLoginEnabled = false;
    private boolean logcatOverlayEnabled = false;
    private boolean lowInputDelayEnabled = false;
    private Boolean crashUploadEnabled = true;
    private boolean foregroundServiceEnabled = false;
    private Boolean gxcoreEnabled = true;

    public enum StorageType {
        INTERNAL,
        EXTERNAL,
        VERSION_ISOLATION,
        VERSION_ISOLATION_INTERNAL,
        VERSION_ISOLATION_EXTERNAL
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static FeatureSettings getInstance() {
        if (INSTANCE == null) {
            synchronized (FeatureSettings.class) {
                if (INSTANCE == null) {
                    INSTANCE = SettingsStorage.load(appContext);
                    if (INSTANCE == null) {
                        INSTANCE = new FeatureSettings();
                    }
                }
            }
        }
        return INSTANCE;
    }

    public boolean isVersionIsolationEnabled() { return versionIsolationEnabled; }
    public void setVersionIsolationEnabled(boolean enabled) { this.versionIsolationEnabled = enabled; autoSave(); }

    public boolean isLauncherManagedMcLoginEnabled() { return launcherManagedMcLoginEnabled; }
    public void setLauncherManagedMcLoginEnabled(boolean enabled) { this.launcherManagedMcLoginEnabled = enabled; autoSave(); }

    public boolean isLogcatOverlayEnabled() { return logcatOverlayEnabled; }
    public void setLogcatOverlayEnabled(boolean enabled) { this.logcatOverlayEnabled = enabled; autoSave(); }

    public boolean isLowInputDelayEnabled() { return lowInputDelayEnabled; }
    public void setLowInputDelayEnabled(boolean enabled) { this.lowInputDelayEnabled = enabled; autoSave(); }

    public boolean isCrashUploadEnabled() { return crashUploadEnabled == null || crashUploadEnabled; }
    public void setCrashUploadEnabled(boolean enabled) { this.crashUploadEnabled = enabled; autoSave(); }

    public boolean isForegroundServiceEnabled() { return foregroundServiceEnabled; }
    public void setForegroundServiceEnabled(boolean enabled) { this.foregroundServiceEnabled = enabled; autoSave(); }

    public boolean isGxCoreEnabled() { return gxcoreEnabled == null || gxcoreEnabled; }
    public void setGxCoreEnabled(boolean enabled) { this.gxcoreEnabled = enabled; autoSave(); }


    private void autoSave() {
        if (appContext != null) {
            SettingsStorage.save(appContext, this);
        }
    }
}
