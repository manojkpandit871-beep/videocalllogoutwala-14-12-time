package com.example.videocallapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages dark/light theme preference.
 */
public class ThemeManager {

    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME  = "theme_mode";

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT  = 1;
    public static final int THEME_DARK   = 2;

    private static ThemeManager instance;
    private final SharedPreferences prefs;

    private ThemeManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static ThemeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) instance = new ThemeManager(context);
            }
        }
        return instance;
    }

    public void applyTheme() {
        int mode = prefs.getInt(KEY_THEME, THEME_SYSTEM);
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public void setTheme(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
        applyTheme();
    }

    public int getCurrentTheme() {
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }
}
