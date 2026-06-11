package com.example.videocallapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import com.example.videocallapp.MainActivity;
import com.example.videocallapp.R;
import com.example.videocallapp.databinding.ActivityLoginBinding;
import com.example.videocallapp.model.UserProfile;
import com.example.videocallapp.utils.ProfileManager;

import java.util.concurrent.TimeUnit;

/**
 * Firebase Authentication screen.
 * Supports: Google Sign-In + Phone OTP login.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ProfileManager profileManager;

    // Phone auth
    private String verificationId;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    showError("Google Sign-In failed: " + e.getMessage());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth    = FirebaseAuth.getInstance();
        profileManager  = ProfileManager.getInstance(this);

        // If already signed in, go to main
        if (firebaseAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google sign-in button
        binding.btnGoogleSignIn.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        // Send OTP button
        binding.btnSendOtp.setOnClickListener(v -> sendOtp());

        // Verify OTP button
        binding.btnVerifyOtp.setOnClickListener(v -> verifyOtp());

        // Toggle phone section
        binding.btnUsePhone.setOnClickListener(v -> {
            boolean visible = binding.layoutPhone.getVisibility() == View.VISIBLE;
            binding.layoutPhone.setVisibility(visible ? View.GONE : View.VISIBLE);
        });
    }

    // ─── Google Sign-In ────────────────────────────────────────────────────────

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            syncFirebaseUser(firebaseAuth.getCurrentUser());
                            goToMain();
                        } else {
                            showError("Authentication failed");
                        }
                    });
    }

    // ─── Phone OTP ─────────────────────────────────────────────────────────────

    private void sendOtp() {
        String phone = binding.etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            binding.etPhoneNumber.setError("Enter valid phone number with country code");
            return;
        }

        showLoading(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        showLoading(false);
                        signInWithPhone(credential);
                    }

                    @Override
                    public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                        showLoading(false);
                        showError("OTP failed: " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(String vId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        showLoading(false);
                        verificationId = vId;
                        binding.layoutOtp.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this,
                                "OTP sent!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String otp = binding.etOtp.getText().toString().trim();
        if (TextUtils.isEmpty(otp) || otp.length() < 6) {
            binding.etOtp.setError("Enter 6-digit OTP");
            return;
        }
        if (TextUtils.isEmpty(verificationId)) {
            showError("Please request OTP first");
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhone(credential);
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        showLoading(true);
        firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            syncFirebaseUser(firebaseAuth.getCurrentUser());
                            goToMain();
                        } else {
                            showError("OTP verification failed");
                        }
                    });
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void syncFirebaseUser(FirebaseUser user) {
        if (user == null) return;
        UserProfile profile = new UserProfile(
                user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getUid(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getUid(),
                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null
        );
        profileManager.saveProfile(profile);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnGoogleSignIn.setEnabled(!show);
        binding.btnSendOtp.setEnabled(!show);
        binding.btnVerifyOtp.setEnabled(!show);
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
