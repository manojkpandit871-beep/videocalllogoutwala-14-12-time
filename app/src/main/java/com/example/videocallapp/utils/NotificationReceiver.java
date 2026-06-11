package com.example.videocallapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver for notification-related actions.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_MISSED_CALL = "com.example.videocallapp.MISSED_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_MISSED_CALL.equals(intent.getAction())) {
            String caller = intent.getStringExtra("callerName");
            String room   = intent.getStringExtra("roomId");
            if (caller != null) {
                NotificationHelper.showMissedCallNotification(context, caller, room);
            }
        }
    }
}
