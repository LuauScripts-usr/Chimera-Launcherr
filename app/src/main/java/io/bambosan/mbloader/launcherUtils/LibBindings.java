package io.bambosan.mbloader.launcherUtils;

/**
 * JNI bindings for mtbinloader2 (mbl2) - shader/materialbin compatibility fixer.
 * This library fixes resource-pack materialbin compatibility across Bedrock versions.
 * Only for 64-bit (arm64-v8a) instances.
 */
public class LibBindings {
    static {
        System.loadLibrary("mtbinloader2");
    }

    /**
     * Set autofix versions for shader compatibility.
     * @param minVersion Minimum Bedrock version string (e.g., "1.20.0")
     * @param maxVersion Maximum Bedrock version string (e.g., "1.21.0")
     */
    public static native void setAutofixVersions(String minVersion, String maxVersion);

    /**
     * Enable or disable lightmap autofixer.
     * @param enabled true to enable, false to disable
     */
    public static native void setLightmapAutofixer(boolean enabled);

    /**
     * Enable or disable texture LOD autofixer.
     * @param enabled true to enable, false to disable
     */
    public static native void setTextureLodAutofixer(boolean enabled);
}
