package com.example.videocallapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.videocallapp.model.UserProfile;

/**
 * Manages local user profile persistence via SharedPreferences.
 */
public class ProfileManager {

    private static final String PREFS_NAME   = "user_profile";
    private static final String KEY_USER_ID  = "userId";
    private static final String KEY_NAME     = "userName";
    private static final String KEY_DISPLAY  = "displayName";
    private static final String KEY_PHOTO    = "photoUri";

    private static ProfileManager instance;
    private final SharedPreferences prefs;

    private ProfileManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static ProfileManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ProfileManager.class) {
                if (instance == null) instance = new ProfileManager(context);
            }
        }
        return instance;
    }

    public void saveProfile(UserProfile profile) {
        prefs.edit()
             .putString(KEY_USER_ID, profile.getUserId())
             .putString(KEY_NAME,    profile.getUserName())
             .putString(KEY_DISPLAY, profile.getDisplayName())
             .putString(KEY_PHOTO,   profile.getPhotoUri())
             .apply();
    }

    public UserProfile loadProfile() {
        return new UserProfile(
                prefs.getString(KEY_USER_ID, ""),
                prefs.getString(KEY_NAME,    ""),
                prefs.getString(KEY_DISPLAY, ""),
                prefs.getString(KEY_PHOTO,   null)
        );
    }

    public boolean hasProfile() {
        return !prefs.getString(KEY_USER_ID, "").isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
