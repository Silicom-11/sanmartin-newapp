package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface LocationDao {

    @POST("location")
    Call<ApiResponse<Location>> sendLocation(@Body Map<String, Object> data);

    @POST("location/logout")
    Call<ApiResponse<Void>> logoutLocation(@Body Map<String, Object> data);

    @POST("location/disconnect")
    Call<ApiResponse<Void>> disconnect(@Body Map<String, Object> data);

    @GET("location/students")
    Call<ApiResponse<List<Location>>> getStudentLocations();

    @GET("location/user/{userId}")
    Call<ApiResponse<Location>> getUserLocation(@Path("userId") String userId);

    @GET("location/user/{userId}/history")
    Call<ApiResponse<List<Location>>> getUserLocationHistory(@Path("userId") String userId);

    @GET("location/children")
    Call<ApiResponse<List<Location>>> getChildrenLocations();

    @GET("location/child/{studentId}")
    Call<ApiResponse<Location>> getChildLocation(@Path("studentId") String studentId);

    @GET("location/stats")
    Call<ApiResponse<Map<String, Object>>> getLocationStats();
}