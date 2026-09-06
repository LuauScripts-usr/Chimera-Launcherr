package org.chimeramc.launcher.launcher.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists up to {@link ControllerProfile#MAX_SLOTS} profiles per controller
 * type in SharedPreferences as JSON, plus the active slot index per type.
 */
public class ControllerProfileManager {
    private static final String PREFS_NAME = "controller_profiles";
    private static final String KEY_LIST_PREFIX = "profiles_";
    private static final String KEY_ACTIVE_PREFIX = "active_";
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<ControllerProfile>>() {}.getType();

    private final SharedPreferences prefs;

    public ControllerProfileManager(Context context) {
        this.prefs = context.getApplicationContext() .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<ControllerProfile> getProfiles(ControllerType type) {
        List<ControllerProfile> profiles = loadList(type);
        if (profiles.isEmpty()) {
            profiles.add(new ControllerProfile("Default"));
            saveList(type, profiles);
        }
        return profiles;
    }

    public void saveProfiles(ControllerType type, List<ControllerProfile> profiles) {
        saveList(type, profiles);
    }

    public int getActiveSlot(ControllerType type) {
        return prefs.getInt(KEY_ACTIVE_PREFIX + type.name() , 0);
    }

    public void setActiveSlot(ControllerType type, int slot) {
        int clamped = Math.max(0, Math.min(ControllerProfile.MAX_SLOTS - 1, slot));
        prefs.edit() .putInt(KEY_ACTIVE_PREFIX + type.name() , clamped).apply();
    }

    public ControllerProfile getActiveProfile(ControllerType type) {
        List<ControllerProfile> profiles = getProfiles(type);
        int slot = getActiveSlot(type);
        return profiles.get(Math.min(slot, profiles.size() - 1));
    }

    public int addProfile(ControllerType type, String name) {
        List<ControllerProfile> profiles = getProfiles(type);
        if (profiles.size() >= ControllerProfile.MAX_SLOTS) {
            return -1;
        }
        ControllerProfile profile = new ControllerProfile(sanitizeName(name, profiles));
        profiles.add(profile);
        saveList(type, profiles);
        return profiles.size() - 1;
    }

    public void deleteProfile(ControllerType type, int slot) {
        List<ControllerProfile> profiles = loadList(type);
        if (slot < 0 || slot >= profiles.size()) return;
        profiles.remove(slot);
        if (profiles.isEmpty()) profiles.add(new ControllerProfile("Default"));
        saveList(type, profiles);
        int active = getActiveSlot(type);
        if (active >= profiles.size()) setActiveSlot(type, profiles.size() - 1);
    }

    public void duplicateProfile(ControllerType type, int slot, String newName) {
        List<ControllerProfile> profiles = getProfiles(type);
        if (slot < 0 || slot >= profiles.size() || profiles.size() >= ControllerProfile.MAX_SLOTS) return;
        ControllerProfile copy = profiles.get(slot) .copy();
        copy.setName(newName == null || newName.trim() .isEmpty() ? copy.getName() + " Copy" : newName.trim());
        profiles.add(copy);
        saveList(type, profiles);
    }

    public void renameProfile(ControllerType type, int slot, String newName) {
        List<ControllerProfile> profiles = getProfiles(type);
        if (slot < 0 || slot >= profiles.size() || newName == null || newName.trim() .isEmpty()) return;
        profiles.get(slot) .setName(newName.trim());
        saveList(type, profiles);
    }

    private String sanitizeName(String name, List<ControllerProfile> profiles) {
        String base = name == null || name.trim() .isEmpty() ? "Profile" : name.trim();
        String candidate = base;
        int idx = 2;
        while (true) {
            final String c = candidate;
            boolean taken = profiles.stream() .anyMatch(p -> p.getName() .equals(c));
            if (!taken) return candidate;
            candidate = base + " " + idx++;
        }
    }

    private List<ControllerProfile> loadList(ControllerType type) {
        String json = prefs.getString(KEY_LIST_PREFIX + type.name() , null);
        if (json == null) return new ArrayList();
        try {
            List<ControllerProfile> profiles = GSON.fromJson(json, LIST_TYPE);
            if (profiles == null) return new ArrayList();
            for (ControllerProfile p : profiles) {
                if (p.getName() == null) p.setName("Profile");
            }
            return profiles;
        } catch (JsonSyntaxException e) {
            return new ArrayList();
        }
    }

    private void saveList(ControllerType type, List<ControllerProfile> profiles) {
        prefs.edit() .putString(KEY_LIST_PREFIX + type.name() , GSON.toJson(profiles)).apply();
    }
}