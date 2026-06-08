package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Justification;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    @Multipart
    @POST("justifications")
    Call<ApiResponse<Justification>> submitJustification(
            @Part("studentId") RequestBody studentId,
            @Part("reason") RequestBody reason,
            @Part("dates") RequestBody dates,
            @Part("observations") RequestBody observations,
            @Part List<MultipartBody.Part> documents);

    @PUT("justifications/{id}/review")
    Call<ApiResponse<Justification>> reviewJustification(
            @Path("id") String id, @Body Map<String, String> data);
}