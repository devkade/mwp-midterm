# Login & Security Feature Testing Guide

## Setup

### Prerequisites
- Django server running on localhost:8000
- Android emulator running
- Test user account created (username: testuser, password: testpass123)

### Server Setup
```bash
cd PhotoBlogServer
python manage.py runserver
```

Create test user in Django shell:
```bash
python manage.py shell
from django.contrib.auth.models import User
User.objects.create_user(username='testuser', password='testpass123')
exit()
```

## Test Cases

### TC1: Initial App Launch (No Stored Token)
**Expected:** SplashActivity shows for 2 seconds, then LoginActivity appears
**Steps:**
1. Uninstall app from emulator
2. Launch app
3. Verify splash screen displays
4. Verify redirects to LoginActivity after 2 seconds

**Result:** ☐ Pass / ☐ Fail

---

### TC2: Valid Login
**Expected:** User logs in successfully and sees photo feed
**Steps:**
1. On LoginActivity, enter username: testuser
2. Enter password: testpass123
3. Click "Log In"
4. Verify navigates to MainActivity with photo feed
5. Verify token is stored securely

**Result:** ☐ Pass / ☐ Fail

---

### TC3: Invalid Credentials
**Expected:** Error message displayed, password cleared
**Steps:**
1. On LoginActivity, enter username: testuser
2. Enter password: wrongpassword
3. Click "Log In"
4. Verify error message appears: "Invalid credentials"
5. Verify password field is cleared
6. Verify username field still has "testuser"

**Result:** ☐ Pass / ☐ Fail

---

### TC4: Empty Fields
**Expected:** Error message displayed
**Steps:**
1. On LoginActivity, leave username empty
2. Click "Log In"
3. Verify error message: "Username and password required"

**Result:** ☐ Pass / ☐ Fail

---

### TC5: Remember Username
**Expected:** Username persists after logout/login cycle
**Steps:**
1. Enter username: testuser and password
2. Check "Remember username" checkbox
3. Click "Log In"
4. Click "Logout" button in MainActivity
5. Verify LoginActivity shows "testuser" in username field
6. Verify checkbox is checked

**Result:** ☐ Pass / ☐ Fail

---

### TC6: Forget Username
**Expected:** Username cleared after logout
**Steps:**
1. Log in with "Remember username" unchecked
2. Click "Logout"
3. Verify username field is empty
4. Verify checkbox is unchecked

**Result:** ☐ Pass / ☐ Fail

---

### TC7: Token Persistence
**Expected:** App goes directly to MainActivity on restart
**Steps:**
1. Log in successfully
2. Close app completely
3. Reopen app
4. Verify SplashActivity briefly shows
5. Verify app goes directly to MainActivity (no login required)

**Result:** ☐ Pass / ☐ Fail

---

### TC8: Logout
**Expected:** Session cleared, returns to LoginActivity
**Steps:**
1. Log in successfully
2. Click "Logout" button
3. Verify navigates to LoginActivity
4. Verify token is deleted from storage
5. Verify cannot view photos without logging in again

**Result:** ☐ Pass / ☐ Fail

---

### TC9: Network Error (Server Down)
**Expected:** Error message displayed
**Steps:**
1. Stop Django server
2. On LoginActivity, enter credentials
3. Click "Log In"
4. Verify error message appears: "Network error: ..."
5. Verify login button becomes enabled again (can retry)

**Result:** ☐ Pass / ☐ Fail

---

### TC10: Token Used in API Calls
**Expected:** Posts are fetched with authentication token
**Steps:**
1. Log in successfully
2. Verify MainActivity displays photos
3. In Android Studio Logcat, verify "Authorization: Token ..." header in requests

**Result:** ☐ Pass / ☐ Fail

---

## Manual Testing Checklist

- [ ] Splash screen appears on startup
- [ ] Login form has username and password fields
- [ ] Login button is visible and clickable
- [ ] Valid credentials allow login
- [ ] Invalid credentials show error
- [ ] Remember username works correctly
- [ ] Logout clears session and token
- [ ] Token persists across app restarts
- [ ] Photo feed loads after successful login
- [ ] Network errors are handled gracefully
- [ ] No hardcoded tokens in code
- [ ] All four API operations (GET, POST, PUT, DELETE) use SessionManager token

---

## Debugging Tips

- Check Logcat for "MainActivity" and "AuthenticationService" logs
- Use Android Studio debugger to inspect SecureTokenManager state
- Check Django server logs for POST /api/auth/login/ requests
- Verify token format in database: `Token.objects.all()` in Django shell
- Inspect shared preferences: Android Studio → Device Explorer → data → com.example.photoviewer → databases

---

## Regression Test (After Each Build)

Run through these core flows to ensure nothing broke:

1. **Cold Start → Login → Browse Feed** (TC1 + TC2)
2. **Logout → Login Again** (TC8 + TC2)
3. **Upload New Photo** (requires valid token)
4. **Edit Existing Photo** (requires valid token)
5. **Delete Photo** (requires valid token)

---

## Known Issues / Limitations

- EncryptedSharedPreferences requires Android SDK 21+
- Token expiration not implemented (use persistent token for now)
- No refresh token mechanism (user must log out and log back in for new token)

---

## Success Criteria

✅ All test cases pass
✅ No crashes during login flow
✅ Token is securely stored and not visible in plain text
✅ All API operations work with SessionManager token
✅ Logout completely clears user session
✅ App properly routes based on authentication state
