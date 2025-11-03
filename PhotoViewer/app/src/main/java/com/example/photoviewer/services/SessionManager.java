package com.example.photoviewer.services;

import com.example.photoviewer.utils.SecureTokenManager;

public class SessionManager {
    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return SecureTokenManager.getInstance().hasToken();
    }

    public String getToken() {
        return SecureTokenManager.getInstance().getToken();
    }

    public String getUsername() {
        return SecureTokenManager.getInstance().getUsername();
    }

    public void saveSession(String username, String token) {
        SecureTokenManager.getInstance().saveUsername(username);
        SecureTokenManager.getInstance().saveToken(token);
    }

    public void logout() {
        SecureTokenManager.getInstance().clearAll();
    }
}
