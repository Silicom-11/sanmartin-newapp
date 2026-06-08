package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.AuthResponse;
import com.iepca.app.model.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Data Access Object for Authentication operations.
 * Implements DAO pattern (rubrica requirement).
 * Each method maps to a backend API endpoint.
 */
public interface AuthDao {

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body Map<String, String> credentials);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body Map<String, Object> userData);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> email);

    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body Map<String, String> data);

    @POST("auth/refresh-token")
    Call<ApiResponse<AuthResponse>> refreshToken(@Body Map<String, String> token);

    @GET("auth/me")
    Call<ApiResponse<User>> getMe();

    @PUT("auth/profile")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, Object> data);

    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body Map<String, String> data);

    @POST("auth/push-token")
    Call<ApiResponse<Void>> registerPushToken(@Body Map<String, String> token);

    @DELETE("auth/push-token")
    Call<ApiResponse<Void>> unregisterPushToken();
}