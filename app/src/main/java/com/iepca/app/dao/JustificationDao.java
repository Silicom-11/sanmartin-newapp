package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Justification;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface JustificationDao {

    @GET("justifications")
    Call<ApiResponse<List<Justification>>> getJustifications(
            @Query("status") String status,
            @Query("studentId") String studentId,
            @Query("page") int page, @Query("limit") int limit);

    @GET("justifications/my-justifications")
    Call<ApiResponse<List<Justification>>> getMyJustifications();

    @GET("justifications/stats")
    Call<ApiResponse<Map<String, Object>>> getJustificationStats();

    @GET("justifications/{id}")
    Call<ApiResponse<Justification>> getJustification(@Path("id") String id);

    @POST("justifications")
    Call<ApiResponse<Justification>> submitJustification(@Body Map<String, Object> data);

    @PUT("justifications/{id}/review")
    Call<ApiResponse<Justification>> reviewJustification(
            @Path("id") String id, @Body Map<String, String> data);
}