package com.example.videocallapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralised runtime permission helper.
 */
public class PermissionHelper {

    public static final int REQUEST_CALL_PERMISSIONS    = 100;
    public static final int REQUEST_STORAGE_PERMISSION  = 101;
    public static final int REQUEST_NOTIFICATION_PERM   = 102;

    /** Returns true when all required call permissions are granted. */
    public static boolean hasCallPermissions(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                   == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                   == PackageManager.PERMISSION_GRANTED;
    }

    /** Request camera + microphone at runtime. */
    public static void requestCallPermissions(Activity activity) {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(activity, perms, REQUEST_CALL_PERMISSIONS);
    }

    /** Request image reading permission (API-version aware). */
    public static void requestStoragePermission(Activity activity) {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(activity, new String[]{perm}, REQUEST_STORAGE_PERMISSION);
    }

    /** Request POST_NOTIFICATIONS on Android 13+. */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERM);
        }
    }

    public static boolean hasStoragePermission(Context ctx) {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED;
    }
}
