package com.iepca.app.config;

/**
 * Application constants.
 * Follows SOLID: Open/Closed - extend via new constants, don't modify existing.
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constants class");
    }

    // API Configuration
    // Resolved per build type: debug uses the local backend through
    // "adb reverse tcp:5000 tcp:5000"; release points to the production server.
    public static final String BASE_URL = com.iepca.app.BuildConfig.API_BASE_URL;
    public static final int CONNECT_TIMEOUT = 30; // seconds
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;

    // SharedPreferences
    public static final String PREFS_NAME = "iepca_prefs";
    public static final String PREF_TOKEN = "auth_token";
    public static final String PREF_REFRESH_TOKEN = "refresh_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_AVATAR = "user_avatar";
    public static final String PREF_FCM_TOKEN = "fcm_token";

    // Roles
    public static final String ROLE_ADMIN = "administrativo";
    public static final String ROLE_TEACHER = "docente";
    public static final String ROLE_PARENT = "padre";
    public static final String ROLE_STUDENT = "estudiante";
    public static final String ROLE_DIRECTOR = "director";

    // GPS Tracking
    public static final long LOCATION_INTERVAL_MS = 30_000; // 30 seconds
    public static final long LOCATION_FASTEST_INTERVAL_MS = 15_000;
    public static final float LOCATION_MIN_DISTANCE_M = 10f;
    public static final int LOCATION_NOTIFICATION_ID = 1001;

    // Attendance Status
    public static final String ATTENDANCE_PRESENT = "present";
    public static final String ATTENDANCE_ABSENT = "absent";
    public static final String ATTENDANCE_LATE = "late";
    public static final String ATTENDANCE_JUSTIFIED = "justified";

    // Justification Status (wire values must match backend — English)
    public static final String JUSTIFICATION_PENDING = "pending";
    public static final String JUSTIFICATION_APPROVED = "approved";
    public static final String JUSTIFICATION_REJECTED = "rejected";

    // Grade Scale
    public static final int GRADE_AD_MIN = 17;
    public static final int GRADE_A_MIN = 14;
    public static final int GRADE_B_MIN = 11;
    public static final int GRADE_C_MIN = 0;
    public static final int GRADE_MAX = 20;
    public static final int GRADE_PASSING = 11;

    // Pagination
    public static final int PAGE_SIZE = 20;

    // Bimesters
    public static final int BIMESTERS_COUNT = 4;

    // Date Formats
    public static final String DATE_FORMAT_API = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_FORMAT = "HH:mm";
}
