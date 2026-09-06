package org.chimeramc.launcher.launcher.controller;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

public class ControllerProfile {
    public static final int MAX_SLOTS = 5;
    public static final float DEFAULT_DEAD_ZONE = 0.15f;
    public static final float DEFAULT_SENSITIVITY = 1.0f;
    public static final float MIN_SENSITIVITY = 0.25f;
    public static final float MAX_SENSITIVITY = 3.0f;

    private String name;
    private final Map<Integer, Integer> buttonRemaps = new HashMap();
    private float leftDeadZone = DEFAULT_DEAD_ZONE;
    private float rightDeadZone = DEFAULT_DEAD_ZONE;
    private float leftStickSensitivity = DEFAULT_SENSITIVITY;
    private float rightStickSensitivity = DEFAULT_SENSITIVITY;
    private boolean vibrationEnabled = true;

    public ControllerProfile() {
        this("Profile");
    }

    public ControllerProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Integer> getButtonRemaps() {
        return buttonRemaps;
    }

    public int remapKey(int keyCode) {
        if (keyCode <=  0) {
            return keyCode;
        }
        Integer mapped = buttonRemaps.get(keyCode);
        return mapped != null ? mapped : keyCode;
    }

    public void setRemap(int fromKeyCode, int toKeyCode) {
        if (fromKeyCode <=  0) {
            return;
        }
        if (toKeyCode <=  0 || toKeyCode == fromKeyCode) {
            buttonRemaps.remove(fromKeyCode);
        } else {
            buttonRemaps.put(fromKeyCode, toKeyCode);
        }
    }

    public float getLeftDeadZone() {
        return leftDeadZone;

    }

    public float getRightDeadZone() {
        return rightDeadZone;

    }

    public float getLeftStickSensitivity() {
        return leftStickSensitivity;

    }

    public float getRightStickSensitivity() {
        return rightStickSensitivity;

    }

    public void setLeftDeadZone(float zone) {
        leftDeadZone = clampDeadZone(zone);
    }

    public void setRightDeadZone(float zone) {
        rightDeadZone = clampDeadZone(zone);
    }

    public void setLeftStickSensitivity(float sensitivity) {
        leftStickSensitivity = clampSensitivity(sensitivity);
    }

    public void setRightStickSensitivity(float sensitivity) {
        rightStickSensitivity = clampSensitivity(sensitivity);
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;

    }

    public void setVibrationEnabled(boolean enabled) {
        vibrationEnabled = enabled;

    }

    private static float clampDeadZone(float zone) {
        if (Float.isNaN(zone)) return DEFAULT_DEAD_ZONE;
        return Math.max(0f, Math.min(0.9f, zone));
    }

    private static float clampSensitivity(float sensitivity) {
        if (Float.isNaN(sensitivity)) return DEFAULT_SENSITIVITY;
        return Math.max(MIN_SENSITIVITY, Math.min(MAX_SENSITIVITY, sensitivity));
    }

    public ControllerProfile copy() {
        ControllerProfile copy = new ControllerProfile(name);
        copy.leftDeadZone = leftDeadZone;

        copy.rightDeadZone = rightDeadZone;

        copy.leftStickSensitivity = leftStickSensitivity;

        copy.rightStickSensitivity = rightStickSensitivity;

        copy.vibrationEnabled = vibrationEnabled;



        copy.buttonRemaps.clear();
        copy.buttonRemaps.putAll(buttonRemaps);
        return copy;

    }

    public static boolean isAxisStick(int axis) {
        return axis == MotionEvent.AXIS_X || axis == MotionEvent.AXIS_Y
                || axis == MotionEvent.AXIS_Z || axis == MotionEvent.AXIS_RZ;
    }
}