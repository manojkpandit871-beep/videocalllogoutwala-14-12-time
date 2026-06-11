package com.example.videocallapp.model;

/**
 * User profile model stored in SharedPreferences.
 */
public class UserProfile {
    private String userId;
    private String userName;
    private String displayName;
    private String photoUri;  // local URI string

    public UserProfile() {}

    public UserProfile(String userId, String userName, String displayName, String photoUri) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.photoUri = photoUri;
    }

    public String getUserId()     { return userId; }
    public String getUserName()   { return userName; }
    public String getDisplayName(){ return displayName; }
    public String getPhotoUri()   { return photoUri; }

    public void setUserId(String userId)         { this.userId = userId; }
    public void setUserName(String userName)     { this.userName = userName; }
    public void setDisplayName(String d)         { this.displayName = d; }
    public void setPhotoUri(String photoUri)     { this.photoUri = photoUri; }
}
