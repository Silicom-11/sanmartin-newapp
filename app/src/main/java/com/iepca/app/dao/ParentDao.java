package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Parent;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ParentDao {

    @GET("parents-management")
    Call<ApiResponse<List<Parent>>> getParents(
            @Query("page") int page, @Query("limit") int limit,
            @Query("search") String search);

    @GET("parents-management/stats")
    Call<ApiResponse<Map<String, Object>>> getParentStats();

    @GET("parents-management/{id}")
    Call<ApiResponse<Parent>> getParent(@Path("id") String id);

    @POST("parents-management")
    Call<ApiResponse<Parent>> createParent(@Body Map<String, Object> data);

    @PUT("parents-management/{id}")
    Call<ApiResponse<Parent>> updateParent(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("parents-management/{id}")
    Call<ApiResponse<Void>> deleteParent(@Path("id") String id);

    @POST("parents-management/{id}/children")
    Call<ApiResponse<Parent>> linkChild(@Path("id") String id, @Body Map<String, String> data);

    @DELETE("parents-management/{id}/children/{studentId}")
    Call<ApiResponse<Parent>> unlinkChild(@Path("id") String id, @Path("studentId") String studentId);

    @POST("parents-management/{id}/reactivate")
    Call<ApiResponse<Parent>> reactivateParent(@Path("id") String id);

    // Parent-facing
    @GET("parent/children")
    Call<ApiResponse<List<Map<String, Object>>>> getMyChildren();

    @GET("parent/children/{childId}/grades")
    Call<ApiResponse<List<Map<String, Object>>>> getChildGrades(@Path("childId") String childId);

    @GET("parent/children/{childId}/attendance")
    Call<ApiResponse<List<Map<String, Object>>>> getChildAttendance(@Path("childId") String childId);
}