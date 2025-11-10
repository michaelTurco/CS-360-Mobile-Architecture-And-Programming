package com.turco_michael_weight_tracking;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {
    // used documentation from
    // https://developer.android.com/training/data-storage/shared-preferences

    // Preference Keys
    private static final String PREFS_NAME = "WeightAppPreferences";
    private static final String KEY_GOAL_WEIGHT = "goal_weight_";
    private static final String KEY_NOTIFICATIONS = "notifications_";
    private static final String KEY_AUTOLOGIN_USERNAME = "autologin_username";
    private static final String KEY_AUTOLOGIN_PASSWORD = "autologin_password";


    // Enum to hold notification states
    public enum NotificationStatus {
        Unknown,
        Accepted,
        Rejected,
    }

    private final SharedPreferences preferences;

    public LocalStorage(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    // Goal Weight
    public void setGoalWeight(float weight) {
        preferences.edit().putFloat(KEY_GOAL_WEIGHT + UserDatabase.currentUsername, weight).apply();
    }

    public float getGoalWeight() {
        return preferences.getFloat(KEY_GOAL_WEIGHT + UserDatabase.currentUsername, -1.0f);
        // -1.0f means it is 'unset'
    }


    // Notification Status
    public void setNotificationStatus(NotificationStatus status) {
        preferences.edit().putInt(KEY_NOTIFICATIONS + UserDatabase.currentUsername, status.ordinal()).apply();
    }

    public NotificationStatus getNotificationStatus() {
        return NotificationStatus.values()[preferences.getInt(KEY_NOTIFICATIONS + UserDatabase.currentUsername, NotificationStatus.Unknown.ordinal())];
        // help on enum casting from
        // https://stackoverflow.com/questions/5878952/cast-int-to-enum-in-java
    }



    // Auto Login Information
    public void setAutoLogin(String username, String password){
        preferences.edit().putString(KEY_AUTOLOGIN_USERNAME, username).apply();
        preferences.edit().putString(KEY_AUTOLOGIN_PASSWORD, password).apply();
    }

    public String getAutoLoginUsername() {
        return preferences.getString(KEY_AUTOLOGIN_USERNAME, null);
    }

    public String getAutoLoginPassword() {
        return preferences.getString(KEY_AUTOLOGIN_PASSWORD, null);
    }
}