package com.example.videocallapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.videocallapp.MainActivity;
import com.example.videocallapp.R;

/**
 * Helper for creating and displaying local notifications (missed calls, etc.).
 */
public class NotificationHelper {

    public static final String CHANNEL_MISSED  = "missed_calls";
    public static final String CHANNEL_CALL    = "active_calls";

    private static final int NOTIF_ID_MISSED = 1001;

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);

        // Missed calls channel
        NotificationChannel missed = new NotificationChannel(
                CHANNEL_MISSED,
                "Missed Calls",
                NotificationManager.IMPORTANCE_HIGH
        );
        missed.setDescription("Notifications for missed video calls");
        nm.createNotificationChannel(missed);

        // Active call channel
        NotificationChannel active = new NotificationChannel(
                CHANNEL_CALL,
                "Active Calls",
                NotificationManager.IMPORTANCE_MAX
        );
        active.setDescription("Incoming call notifications");
        nm.createNotificationChannel(active);
    }

    /**
     * Show a missed-call notification.
     * @param callerName  Name of the caller who was missed
     * @param roomId      Room ID of the call
     */
    public static void showMissedCallNotification(Context context, String callerName, String roomId) {
        // Tap notification → open MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MISSED)
                .setSmallIcon(R.drawable.ic_call_missed)
                .setContentTitle("Missed Video Call")
                .setContentText("You missed a call from " + callerName)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_MISSED, builder.build());
        } catch (SecurityException e) {
            // Permission not granted – silently skip
        }
    }
}
