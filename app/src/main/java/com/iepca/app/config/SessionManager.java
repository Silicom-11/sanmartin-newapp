package com.iepca.app.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages user session (JWT token, user data) in SharedPreferences.
 * Implements Singleton pattern. Follows SOLID: Single Responsibility.
 */
public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // --- Token Management ---

    public void saveToken(String token) {
        prefs.edit().putString(Constants.PREF_TOKEN, token).apply();
        LOG.debug("Token saved");
    }

    public String getToken() {
        return prefs.getString(Constants.PREF_TOKEN, null);
    }

    public void saveRefreshToken(String token) {
        prefs.edit().putString(Constants.PREF_REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.PREF_REFRESH_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // --- User Data ---

    public void saveUserData(String id, String email, String name, String role, String avatar) {
        prefs.edit()
                .putString(Constants.PREF_USER_ID, id)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_NAME, name)
                .putString(Constants.PREF_USER_ROLE, role)
                .putString(Constants.PREF_USER_AVATAR, avatar)
                .apply();
        LOG.info("User data saved: {} ({})", email, role);
    }

    public void saveStudentProfile(String studentProfileId) {
        prefs.edit().putString("student_profile_id", studentProfileId != null ? studentProfileId : "").apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.PREF_USER_ID, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "");
    }

    public String getUserRole() {
        return prefs.getString(Constants.PREF_USER_ROLE, "");
    }

    public String getUserAvatar() {
        return prefs.getString(Constants.PREF_USER_AVATAR, "");
    }

    public String getStudentProfileId() {
        return prefs.getString("student_profile_id", "");
    }

    // --- FCM Token ---

    public void saveFcmToken(String token) {
        prefs.edit().putString(Constants.PREF_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return prefs.getString(Constants.PREF_FCM_TOKEN, "");
    }

    // --- Session Control ---

    public void clearSession() {
        prefs.edit().clear().apply();
        LOG.info("Session cleared");
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(getUserRole()) ||
               Constants.ROLE_DIRECTOR.equals(getUserRole());
    }

    public boolean isTeacher() {
        return Constants.ROLE_TEACHER.equals(getUserRole());
    }

    public boolean isParent() {
        return Constants.ROLE_PARENT.equals(getUserRole());
    }

    public boolean isStudent() {
        return Constants.ROLE_STUDENT.equals(getUserRole());
    }
}
