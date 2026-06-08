package com.iepca.app.model;

/**
 * Authentication response with JWT token and user data.
 */
public class AuthResponse {
    private String token;
    private String refreshToken;
    private User user;

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public User getUser() { return user; }
}
