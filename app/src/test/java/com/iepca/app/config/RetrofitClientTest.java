package com.iepca.app.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class RetrofitClientTest {

    @Test
    public void baseUrlShouldNotBeNull() {
        assertNotNull(Constants.BASE_URL);
    }

    @Test
    public void baseUrlShouldNotContainProductionSecrets() {
        assertFalse(Constants.BASE_URL.contains("password"));
        assertFalse(Constants.BASE_URL.contains("secret"));
        assertFalse(Constants.BASE_URL.contains("apikey"));
    }

    @Test
    public void connectTimeoutShouldBeReasonable() {
        assertTrue(Constants.CONNECT_TIMEOUT >= 10);
        assertTrue(Constants.CONNECT_TIMEOUT <= 60);
    }

    @Test
    public void readTimeoutShouldBeReasonable() {
        assertTrue(Constants.READ_TIMEOUT >= 10);
        assertTrue(Constants.READ_TIMEOUT <= 60);
    }

    @Test
    public void writeTimeoutShouldBeReasonable() {
        assertTrue(Constants.WRITE_TIMEOUT >= 10);
        assertTrue(Constants.WRITE_TIMEOUT <= 60);
    }

    @Test
    public void baseUrlShouldContainApiPath() {
        assertTrue(Constants.BASE_URL.contains("/api/"));
    }
}
