package org.chimeramc.launcher.launcher.controller;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;

import java.util.ArrayList;
import java.util.List;

public class ControllerManager {
    public interface Listener {
        void onConnectedControllerChanged(ControllerType type);
    }

    private final InputManager inputManager;
    private final List<Listener> listeners = new ArrayList<>();
    private ControllerType current;

    public ControllerManager(Context context){
        this.inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
    }

    private final InputManager.InputDeviceListener deviceListener = new InputManager.InputDeviceListener() {
        @Override
        public void onInputDeviceAdded(int deviceId){
            refresh();
        }
        @Override
        public void onInputDeviceRemoved(int deviceId){
            refresh();
        }
        @Override
        public void onInputDeviceChanged(int deviceId){
            refresh();
        }
    };

    public void register(){
        refresh();
        inputManager.registerInputDeviceListener(deviceListener, null);
    }

    public void unregister(){
        inputManager.unregisterInputDeviceListener(deviceListener);
    }

    public ControllerType getConnectedType(){
        return current;
    }

    public void addListener(Listener l){
        if(!listeners.contains(l)){
            listeners.add(l);
        }
        l.onConnectedControllerChanged(current);
    }

    public void removeListener(Listener l){
        listeners.remove(l);
    }

    private void refresh(){
        ControllerType found = null;
        int[] ids = inputManager.getInputDeviceIds();
        if(ids != null){
            for (int id : ids){
                InputDevice device = inputManager.getInputDevice(id);
                if(device != null){
                    ControllerType t = ControllerType.from(device);
                    if(t != null){
                        found = t;
                        break;
                    }
                }
            }
        }
        if(found != current){
            current = found;
            for (Listener l : listeners){
                l.onConnectedControllerChanged(current);
            }
        }
    }
}
