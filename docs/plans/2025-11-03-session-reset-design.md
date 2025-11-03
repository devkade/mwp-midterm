# Session Reset Design

**Date:** 2025-11-03
**Status:** Approved

## Overview

Implement automatic session clearing when the app is force-closed or inactive for 10 minutes. This ensures users must re-authenticate after these events while maintaining session persistence for normal app usage.

## Requirements

1. **Force Close Detection:** When user swipes app from recents (process killed), require login on next startup
2. **Inactivity Timeout:** Clear session after 10 minutes of inactivity
3. **Normal Usage:** Keep session active when app is reopened within 10 minutes
4. **Data to Clear:** Auth token and user profile information

## Technical Approach

### Dual Detection Mechanism

We use two mechanisms to detect when session should be cleared:

1. **Process Alive Flag** (`session_active` boolean)
   - Set to `true` when app is in foreground
   - Cleared when process is destroyed
   - On startup, if missing/false = process was killed = clear session

2. **Last Active Timestamp** (`last_active_time` long)
   - Updated when app goes to background
   - On startup, check if (current_time - last_active_time) > 10 minutes
   - If yes = inactivity timeout = clear session

### Session Check Logic

```
App Startup:
├─ Read session_active flag
├─ If flag is false/missing:
│  └─ Process was killed → Clear all session data
└─ If flag is true:
   ├─ Read last_active_time
   ├─ If (current_time - last_active_time) > 10 min:
   │  └─ Clear all session data (inactivity timeout)
   └─ Else: Keep session (user still logged in)
```

## Implementation Components

### 1. Application Class (New: `PhotoViewerApplication.java`)

**Purpose:** Central lifecycle management for the entire application

**Responsibilities:**
- Register `ActivityLifecycleCallbacks` to track app foreground/background state
- Perform session validity check in `onCreate()`
- Update `session_active` flag and `last_active_time` timestamp
- Trigger session clearing when conditions are met

**Key Methods:**
- `onCreate()` - Check session validity on startup
- `onActivityStarted()` - Set session_active = true (app in foreground)
- `onActivityStopped()` - Update last_active_time, track if all activities stopped
- Lifecycle counter to detect when ALL activities are stopped

### 2. SecureTokenManager Updates

**New Methods:**
- `setSessionActive(boolean active)` - Set/clear the session_active flag
- `isSessionActive()` - Check if session is currently active
- `setLastActiveTime(long timestamp)` - Store last active timestamp
- `getLastActiveTime()` - Retrieve last active timestamp
- `clearSession()` - Clear all session data (token + profile + flags)

**SharedPreferences Keys:**
- `auth_token` - Existing auth token
- `session_active` - Boolean flag for process state
- `last_active_time` - Long timestamp (System.currentTimeMillis())

### 3. MainActivity Updates

**Changes:**
- Add session check in `onCreate()` or `onResume()`
- If session is invalid, redirect to `LoginActivity`
- Clear any cached user data when redirecting

### Constants

```java
public static final long SESSION_TIMEOUT_MS = 600000; // 10 minutes
```

## Data to Clear

When `clearSession()` is called, remove:
1. Auth token (`auth_token`)
2. User profile information (if stored)
3. Session flags (`session_active`, `last_active_time`)

## Edge Cases

### 1. First App Launch
- No timestamps/flags exist
- Should NOT clear session (nothing to clear)
- Initialize `session_active = true` and set initial timestamp

### 2. App Crash
- Process killed unexpectedly
- `session_active` flag will be false on next startup
- Session correctly cleared

### 3. System-Initiated Process Kill
- Android kills app for memory management
- Same as crash - flag absent/false, session cleared

### 4. Quick Background/Foreground Cycles
- User switches apps briefly (< 10 min)
- Timestamp updated, but within timeout window
- Session persists correctly

## Testing Scenarios

### Test 1: Force Close Detection
1. Login to app successfully
2. Force close app (swipe from recent apps)
3. Reopen app
4. **Expected:** Redirected to login screen

### Test 2: Inactivity Timeout
1. Login to app successfully
2. Press home button (app goes to background)
3. Wait 11 minutes
4. Reopen app
5. **Expected:** Redirected to login screen

### Test 3: Quick Switch (Session Persistence)
1. Login to app successfully
2. Press home button
3. Wait 2 minutes
4. Reopen app
5. **Expected:** Still logged in, main screen shown

### Test 4: Normal Usage
1. Login to app successfully
2. Use app normally
3. Close and reopen within 10 minutes
4. **Expected:** Still logged in

### Test 5: First Launch
1. Fresh install of app
2. App starts for first time
3. **Expected:** Shows login screen, no crashes

## Security Considerations

1. **SharedPreferences Security:** Currently using standard SharedPreferences. Consider using EncryptedSharedPreferences for production.
2. **Timeout Duration:** 10 minutes provides balance between security and user convenience. Adjust based on security requirements.
3. **Token Storage:** Auth token should never be logged or exposed in error messages.

## Implementation Order

1. Create `PhotoViewerApplication` class with lifecycle callbacks
2. Update `SecureTokenManager` with new session methods
3. Update `MainActivity` to check session validity
4. Test all scenarios thoroughly
5. Update manifest to register Application class

## Success Criteria

- Force-closed app requires re-login on next startup
- 10+ minute inactivity triggers session clear
- Normal usage within 10 minutes maintains session
- No crashes or data loss during session transitions
- All test scenarios pass consistently
