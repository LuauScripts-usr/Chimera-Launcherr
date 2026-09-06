package org.chimeramc.launcher.launcher.controller;

import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.Map;

public final class ControllerInputProcessor {
    private static ControllerProfile activeProfile;
    private static ControllerType activeType;

    private ControllerInputProcessor() {
    }

    public static synchronized void setActiveProfile(ControllerType type, ControllerProfile profile) {
        activeType = type;
        activeProfile = profile;
    }

    public static synchronized ControllerType getActiveType() {
        return activeType;
    }

    public static synchronized boolean isActive() {
        return activeProfile != null;
    }

    public static synchronized void detectAndLoad(Context context) {
        int[] ids = InputDevice.getDeviceIds();
        for (int id : ids) {
            InputDevice device = InputDevice.getDevice(id);
            if (device == null) continue;
            int sources = device.getSources();
            if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                ControllerType type = ControllerType.from(device);
                if (type != null) {
                    ControllerProfile profile = new ControllerProfileManager(context).getActiveProfile(type);
                    setActiveProfile(type, profile);
                    return;
                }
            }
        }
    }

    public static synchronized int remapKey(int keyCode) {
        if (activeProfile == null || keyCode <=  0) {
            return keyCode;
        }
        Map<Integer, Integer> remaps = activeProfile.getButtonRemaps();
        Integer mapped = remaps.get(keyCode);
        return mapped != null ? mapped : keyCode;
    }

    public static synchronized float adjustAxisValue(int axis, float value) {
        if (activeProfile == null) {
            return value;
        }
        boolean left = axis == MotionEvent.AXIS_X || axis == MotionEvent.AXIS_Y;
        boolean right = axis == MotionEvent.AXIS_Z || axis == MotionEvent.AXIS_RZ;
        if (!left && !right) {
            return value;
        }
        float deadZone = left ? activeProfile.getLeftDeadZone() : activeProfile.getRightDeadZone();
        float sensitivity = left ? activeProfile.getLeftStickSensitivity() : activeProfile.getRightStickSensitivity();
        float dead = Math.abs(value);
        if (dead <= deadZone) {
            return  0f;
        }
        float scaled = (dead - deadZone) / Math.max(0.0001f, 1.0f - deadZone);
        scaled = Math.min(1.0f, scaled) * sensitivity;
        scaled = Math.min(1.0f, Math.max(-1.0f, scaled));
        return value <  0f ? -scaled : scaled;
    }

    public static synchronized boolean isWithinDeadZone(MotionEvent event) {
        if (activeProfile == null || event == null) {
            return false;
        }
        int sources = event.getSource();
        if ((sources & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK
                && (sources & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD) {
            return false;
        }
        int[] axes = {MotionEvent.AXIS_X, MotionEvent.AXIS_Y, MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ};
        for (int axis : axes) {
            float value = event.getAxisValue(axis);
            float adjusted = adjustAxisValue(axis, value);
            if (Math.abs(value) > 0.01f && adjusted == 0f) {
                return true;
            }
        }
        return false;
    }

    public static synchronized int processKeyEvent(int keyCode) {
        if (activeProfile == null || keyCode <= 0) {
            return keyCode;
        }
        return remapKey(keyCode);
    }
}