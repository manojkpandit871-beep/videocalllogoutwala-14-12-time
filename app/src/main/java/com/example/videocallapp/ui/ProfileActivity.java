package com.example.videocallapp.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.videocallapp.R;
import com.example.videocallapp.databinding.ActivityProfileBinding;
import com.example.videocallapp.model.UserProfile;
import com.example.videocallapp.utils.PermissionHelper;
import com.example.videocallapp.utils.ProfileManager;

/**
 * Screen for editing user profile: name + profile photo.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ProfileManager profileManager;
    private Uri selectedPhotoUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedPhotoUri = result.getData().getData();
                    // Persist read permission across reboots
                    if (selectedPhotoUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    selectedPhotoUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException ignored) {}

                        Glide.with(this)
                             .load(selectedPhotoUri)
                             .circleCrop()
                             .into(binding.imgProfilePhoto);
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openGallery();
                else Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileManager = ProfileManager.getInstance(this);

        // Load existing profile
        UserProfile profile = profileManager.loadProfile();
        binding.etDisplayName.setText(profile.getDisplayName());
        binding.etUserId.setText(profile.getUserId());

        if (profile.getPhotoUri() != null && !profile.getPhotoUri().isEmpty()) {
            Glide.with(this)
                 .load(Uri.parse(profile.getPhotoUri()))
                 .circleCrop()
                 .placeholder(R.drawable.ic_person_placeholder)
                 .into(binding.imgProfilePhoto);
        }

        // Pick photo from gallery
        binding.btnChangePhoto.setOnClickListener(v -> {
            if (PermissionHelper.hasStoragePermission(this)) {
                openGallery();
            } else {
                String perm = android.os.Build.VERSION.SDK_INT >= 33
                        ? Manifest.permission.READ_MEDIA_IMAGES
                        : Manifest.permission.READ_EXTERNAL_STORAGE;
                requestPermLauncher.launch(perm);
            }
        });

        // Save profile
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(intent);
    }

    private void saveProfile() {
        String displayName = binding.etDisplayName.getText().toString().trim();
        String userId      = binding.etUserId.getText().toString().trim();

        if (TextUtils.isEmpty(userId)) {
            binding.etUserId.setError("User ID is required");
            return;
        }

        UserProfile profile = new UserProfile(
                userId,
                userId,
                displayName.isEmpty() ? userId : displayName,
                selectedPhotoUri != null ? selectedPhotoUri.toString()
                        : profileManager.loadProfile().getPhotoUri()
        );
        profileManager.saveProfile(profile);
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
