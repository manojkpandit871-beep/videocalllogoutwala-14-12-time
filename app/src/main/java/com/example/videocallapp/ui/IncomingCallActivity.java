package com.example.videocallapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.videocallapp.CallActivity;
import com.example.videocallapp.R;
import com.example.videocallapp.databinding.ActivityIncomingCallBinding;
import com.example.videocallapp.utils.NotificationHelper;

/**
 * WhatsApp-style incoming call screen.
 * Launched by a notification or internal intent.
 *
 * Extras:
 *   callerName  – display name of the caller
 *   callerPhoto – URI string of caller's photo (optional)
 *   roomId      – ZEGOCLOUD room/call ID
 *   userId      – local user ID
 */
public class IncomingCallActivity extends AppCompatActivity {

    public static final String EXTRA_CALLER_NAME  = "callerName";
    public static final String EXTRA_CALLER_PHOTO = "callerPhoto";
    public static final String EXTRA_ROOM_ID      = "roomId";
    public static final String EXTRA_USER_ID      = "userId";

    private ActivityIncomingCallBinding binding;
    private String callerName;
    private String roomId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Show over lock screen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
              | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
              | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
              | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.onCreate(savedInstanceState);
        binding = ActivityIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Read extras
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);
        String callerPhoto = getIntent().getStringExtra(EXTRA_CALLER_PHOTO);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        userId = getIntent().getStringExtra(EXTRA_USER_ID);

        if (callerName == null) callerName = "Unknown";

        binding.tvCallerName.setText(callerName);
        binding.tvCallStatus.setText("Incoming Video Call…");

        // Load caller photo
        if (callerPhoto != null && !callerPhoto.isEmpty()) {
            Glide.with(this)
                 .load(Uri.parse(callerPhoto))
                 .circleCrop()
                 .placeholder(R.drawable.ic_person_placeholder)
                 .into(binding.imgCaller);
        } else {
            binding.imgCaller.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Accept call
        binding.btnAccept.setOnClickListener(v -> {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("userID",   userId);
            intent.putExtra("userName", userId + "_Name");
            intent.putExtra("callID",   roomId);
            startActivity(intent);
            finish();
        });

        // Decline call
        binding.btnDecline.setOnClickListener(v -> {
            // Show missed call notification for the caller
            NotificationHelper.showMissedCallNotification(this, callerName, roomId);
            finish();
        });
    }
}
