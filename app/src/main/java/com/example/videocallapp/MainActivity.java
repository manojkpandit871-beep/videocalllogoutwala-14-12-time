package com.example.videocallapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.bumptech.glide.Glide;
import com.example.videocallapp.databinding.ActivityMainBinding;
import com.example.videocallapp.model.UserProfile;
import com.example.videocallapp.ui.CallHistoryActivity;
import com.example.videocallapp.ui.LoginActivity;
import com.example.videocallapp.ui.ProfileActivity;
import com.example.videocallapp.utils.NotificationHelper;
import com.example.videocallapp.utils.PermissionHelper;
import com.example.videocallapp.utils.ProfileManager;
import com.example.videocallapp.utils.ThemeManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ProfileManager profileManager;
    private ThemeManager themeManager;

    // ── Logout: new fields ────────────────────────────────────────────────────
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen BEFORE super.onCreate
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Apply saved theme
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Toolbar
        setSupportActionBar(binding.toolbar);

        profileManager = ProfileManager.getInstance(this);

        // ── Logout: initialise Firebase + Google Sign-In client ───────────────
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        // ─────────────────────────────────────────────────────────────────────

        // Create notification channels
        NotificationHelper.createChannels(this);

        // Request permissions
        if (!PermissionHelper.hasCallPermissions(this)) {
            PermissionHelper.requestCallPermissions(this);
        }
        PermissionHelper.requestNotificationPermission(this);

        // Load existing profile
        loadProfile();

        // Pre-fill a random User ID if empty
        if (TextUtils.isEmpty(binding.etUserId.getText())) {
            binding.etUserId.setText("User_" + (int)(Math.random() * 9000 + 1000));
        }

        // Start Video Call
        binding.btnStartCall.setOnClickListener(v -> startCall());

        // Profile avatar click → open profile screen
        binding.imgProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        UserProfile profile = profileManager.loadProfile();
        if (profile.getPhotoUri() != null && !profile.getPhotoUri().isEmpty()) {
            Glide.with(this)
                 .load(Uri.parse(profile.getPhotoUri()))
                 .placeholder(R.drawable.ic_person_placeholder)
                 .circleCrop()
                 .into(binding.imgProfile);
        } else {
            binding.imgProfile.setImageResource(R.drawable.ic_person_placeholder);
        }

        if (!TextUtils.isEmpty(profile.getDisplayName())) {
            binding.tvWelcome.setText("Hello, " + profile.getDisplayName() + "!");
        }

        // Pre-fill saved username if present
        if (!TextUtils.isEmpty(profile.getUserId())
                && TextUtils.isEmpty(binding.etUserId.getText())) {
            binding.etUserId.setText(profile.getUserId());
        }
    }

    private void startCall() {
        String userId = binding.etUserId.getText().toString().trim();
        String callId = binding.etCallId.getText().toString().trim();

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Please enter User ID", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(callId)) {
            Toast.makeText(this, "Please enter Call Room ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user ID to profile
        UserProfile profile = profileManager.loadProfile();
        if (TextUtils.isEmpty(profile.getUserId())) {
            profile.setUserId(userId);
            profile.setUserName(userId);
            profile.setDisplayName(userId);
            profileManager.saveProfile(profile);
        }

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("userID",   userId);
        intent.putExtra("userName", userId + "_Name");
        intent.putExtra("callID",   callId);
        startActivity(intent);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivity(new Intent(this, CallHistoryActivity.class));
            return true;

        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;

        } else if (id == R.id.action_theme) {
            showThemeDialog();
            return true;

        } else if (id == R.id.action_logout) {          // ← REQ #1
            showLogoutConfirmationDialog();              // ← REQ #2
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ── Theme dialog (unchanged) ──────────────────────────────────────────────

    private void showThemeDialog() {
        String[] options = {"System Default", "Light", "Dark"};
        int current = themeManager.getCurrentTheme();

        new AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(options, current, (dialog, which) -> {
                    themeManager.setTheme(which);
                    dialog.dismiss();
                    recreate();
                })
                .show();
    }

    // ── Logout implementation ─────────────────────────────────────────────────

    /**
     * REQ #2 — confirmation dialog before signing out.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setPositiveButton(R.string.logout_dialog_positive,
                        (dialog, which) -> performLogout())
                .setNegativeButton(R.string.logout_dialog_negative, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Full sign-out sequence:
     *
     *  Step 1 – REQ #3  : FirebaseAuth.getInstance().signOut()
     *  Step 2 – REQ #4  : GoogleSignInClient.signOut()   (async, chained)
     *  Step 3 – REQ #5  : ProfileManager.clear()  →  wipes all SharedPrefs
     *  Step 4 – REQ #6  : FLAG_ACTIVITY_CLEAR_TASK destroys CallActivity
     *                      (and every other activity in the back stack)
     *                      before LoginActivity starts, so any live ZEGO
     *                      session is terminated via CallActivity.onDestroy()
     *  Step 5 – REQ #7  : startActivity(LoginActivity)
     *  Step 6 – REQ #8+9: FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
     *                      empties the task back stack; back button lands
     *                      nowhere (app exits), never returns to MainActivity.
     */
    private void performLogout() {

        // REQ #3 – Firebase sign-out (synchronous)
        firebaseAuth.signOut();

        // REQ #4 – Google sign-out (async); all subsequent steps run in callback
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {

            // REQ #5 – wipe all locally stored user data
            profileManager.clear();

            // REQ #6, #7, #8, #9
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK   // start a new task
                  | Intent.FLAG_ACTIVITY_CLEAR_TASK // destroy every activity
            );                                      // in the existing task,
            startActivity(intent);                  // including CallActivity
            finish();                               // (closes ZEGO session
                                                    //  via onDestroy)
        });
    }
}
