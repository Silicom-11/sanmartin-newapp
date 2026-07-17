package com.iepca.app.security;

import com.iepca.app.config.Constants;

import org.junit.Test;
import static org.junit.Assert.*;

public class SessionSecurityTest {

    @Test
    public void tokenKeyShouldNotBeEmpty() {
        assertNotNull(Constants.PREF_TOKEN);
        assertFalse(Constants.PREF_TOKEN.isEmpty());
    }

    @Test
    public void refreshTokenKeyShouldNotBeEmpty() {
        assertNotNull(Constants.PREF_REFRESH_TOKEN);
        assertFalse(Constants.PREF_REFRESH_TOKEN.isEmpty());
    }

    @Test
    public void preferencesNameShouldBeSecure() {
        assertNotNull(Constants.PREFS_NAME);
        assertFalse(Constants.PREFS_NAME.contains(" "));
        assertFalse(Constants.PREFS_NAME.contains("/"));
    }

    @Test
    public void tokenAndRefreshTokenKeysShouldBeDifferent() {
        assertNotEquals(Constants.PREF_TOKEN, Constants.PREF_REFRESH_TOKEN);
    }

    @Test
    public void allPreferenceKeysShouldBeUnique() {
        String[] keys = {
                Constants.PREF_TOKEN,
                Constants.PREF_REFRESH_TOKEN,
                Constants.PREF_USER_ID,
                Constants.PREF_USER_EMAIL,
                Constants.PREF_USER_NAME,
                Constants.PREF_USER_ROLE,
                Constants.PREF_USER_AVATAR,
                Constants.PREF_FCM_TOKEN
        };
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals("Duplicate preference key found: " + keys[i],
                        keys[i], keys[j]);
            }
        }
    }

    @Test
    public void baseUrlShouldUseCorrectProtocol() {
        assertTrue(Constants.BASE_URL.startsWith("http"));
    }

    @Test
    public void baseUrlShouldEndWithApiSlash() {
        assertTrue(Constants.BASE_URL.endsWith("/api/"));
    }

    @Test
    public void timeoutsShouldBePositive() {
        assertTrue(Constants.CONNECT_TIMEOUT > 0);
        assertTrue(Constants.READ_TIMEOUT > 0);
        assertTrue(Constants.WRITE_TIMEOUT > 0);
    }

    @Test
    public void timeoutsShouldNotExceedReasonableLimit() {
        assertTrue(Constants.CONNECT_TIMEOUT <= 60);
        assertTrue(Constants.READ_TIMEOUT <= 60);
        assertTrue(Constants.WRITE_TIMEOUT <= 60);
    }
}
