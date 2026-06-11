package com.example.videocallapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videocallapp.db.AppDatabase;
import com.example.videocallapp.model.CallRecord;
import com.example.videocallapp.utils.CallTimer;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the ZEGOCLOUD call fragment.
 * Adds call timer overlay and saves call record to Room DB on completion.
 *
 * REQ #6 – When MainActivity.performLogout() fires FLAG_ACTIVITY_CLEAR_TASK,
 * Android calls finish() on every activity in the stack, which triggers
 * onDestroy() here.  onDestroy() stops the timer, removes the ZEGO fragment
 * (ending the ZEGO session), and persists the call record — so the video call
 * is closed safely regardless of whether the user hangs up manually or logs out.
 */
public class CallActivity extends AppCompatActivity {

    // ⚠️ Replace with your own credentials from https://console.zegocloud.com
    private static final long   APP_ID   = 12689723L;
    private static final String APP_SIGN = "ebd09eca9e87dbaa55b3fd8cb152b175a329c4ae577bfc547eb1c4d0ac464b79";

    private CallTimer  timer;
    private TextView   tvCallTimer;
    private long       callStartTime;
    private String     userID;
    private String     callID;

    /** Tag used to find/remove the ZEGO fragment during forced logout. */
    private static final String TAG_ZEGO_FRAGMENT = "zego_call_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        tvCallTimer = findViewById(R.id.tv_call_timer);

        userID          = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        callID          = getIntent().getStringExtra("callID");

        addCallFragment(userID, userName, callID);
    }

    private void addCallFragment(String userID, String userName, String callID) {
        ZegoUIKitPrebuiltCallConfig config =
                ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();

        ZegoUIKitPrebuiltCallFragment fragment =
                ZegoUIKitPrebuiltCallFragment.newInstance(
                        APP_ID, APP_SIGN, userID, userName, callID, config);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, TAG_ZEGO_FRAGMENT)
                .commitNow();

        // Start timer after fragment is attached
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            callStartTime = System.currentTimeMillis();
            timer = new CallTimer(tvCallTimer);
            tvCallTimer.setVisibility(View.VISIBLE);
            timer.start();
        }, 1000);
    }

    /**
     * Called both on normal hang-up AND when FLAG_ACTIVITY_CLEAR_TASK destroys
     * this activity during logout (REQ #6).
     *
     * Stops the timer → removes ZEGO fragment (tears down ZEGO session) →
     * persists call record.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop timer and capture duration
        long duration = 0;
        if (timer != null) {
            duration = timer.getElapsedSeconds();
            timer.stop();
        }

        // REQ #6 – safely close ZEGO session by removing the fragment.
        // If the fragment manager is still valid (not already committed to
        // a destroyed state), detach the fragment so ZEGO can release its
        // resources (camera, mic, network connection).
        try {
            androidx.fragment.app.Fragment zegoFragment =
                    getSupportFragmentManager().findFragmentByTag(TAG_ZEGO_FRAGMENT);
            if (zegoFragment != null && !getSupportFragmentManager().isDestroyed()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .remove(zegoFragment)
                        .commitAllowingStateLoss();
            }
        } catch (Exception ignored) {
            // Fragment manager already destroyed – ZEGO releases on its own.
        }

        saveCallRecord(duration);
    }

    private void saveCallRecord(long durationSeconds) {
        String type = durationSeconds > 0 ? "OUTGOING" : "MISSED";
        CallRecord record = new CallRecord(
                userID  != null ? userID  : "Unknown",
                callID  != null ? callID  : "Unknown",
                System.currentTimeMillis(),
                durationSeconds,
                type
        );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase.getInstance(getApplicationContext())
                       .callDao()
                       .insert(record);
            executor.shutdown();
        });
    }
}
