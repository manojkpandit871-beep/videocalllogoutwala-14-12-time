# VideoCall App — Upgraded

## What's New (All 4 Phases)

### Phase 1
- ✅ Custom app logo (all mipmap densities + adaptive icon)
- ✅ Splash screen using `androidx.core:core-splashscreen`
- ✅ User profile: display name, User ID, profile photo (from gallery)
- ✅ Profile photo shown on home screen
- ✅ Call timer overlay in HH:MM:SS on call screen
- ✅ Dark / Light / System theme with manual toggle + saved preference

### Phase 2
- ✅ Call History screen backed by Room DB
- ✅ Stores: user name, room ID, date, call duration, type (OUTGOING/INCOMING/MISSED)
- ✅ Clear all history button
- ✅ WhatsApp-style Incoming Call screen (accept / decline)
- ✅ Missed call local notification

### Phase 3
- ✅ Firebase Authentication integration
- ✅ Google Sign-In
- ✅ Phone Number OTP login with Firebase
- ✅ Profile sync from Firebase user info

### Phase 4 *(architecture ready; activate via config)*
- ✅ ZEGOCLOUD supports group calls — switch config to `groupVideoCall()` in CallActivity
- ✅ Screen sharing available via ZEGOCLOUD SDK

---

## Setup Steps

### 1. ZEGOCLOUD Credentials
In `CallActivity.java`, replace:
```java
private static final long   APP_ID   = 12689723L;
private static final String APP_SIGN = "ebd09eca9e87dbaa55b3fd8cb152b175a329c4ae577bfc547eb1c4d0ac464b79";
```
with your own credentials from https://console.zegocloud.com

### 2. Firebase Setup
1. Create a project at https://console.firebase.google.com
2. Add an Android app with package `com.example.videocallapp`
3. Download `google-services.json` and replace `app/google-services.json`
4. Enable **Google Sign-In** and **Phone Authentication** in Firebase Console
5. Copy your Web Client ID and update `strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
   ```
6. Add your phone number's country to the Firebase Phone Auth allowlist for testing.

### 3. Enable Group Calls (Phase 4)
In `CallActivity.java` line:
```java
ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
```
Change to:
```java
ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.groupVideoCall();
```

### 4. Build
```bash
./gradlew assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Testing Checklist
- [ ] App icon appears correctly on launcher
- [ ] Splash screen shows on launch
- [ ] Profile photo can be picked from gallery and persists
- [ ] Theme toggle works (System / Light / Dark) and survives restart
- [ ] Start a call — timer shows HH:MM:SS overlay
- [ ] End a call — record appears in Call History
- [ ] Simulate incoming call: `adb shell am start -n com.example.videocallapp/.ui.IncomingCallActivity --es callerName "Alice" --es roomId "test-room" --es userId "user123"`
- [ ] Decline incoming call → missed call notification appears
- [ ] Google Sign-In works (requires real `google-services.json`)
- [ ] Phone OTP login works (requires real Firebase project)
