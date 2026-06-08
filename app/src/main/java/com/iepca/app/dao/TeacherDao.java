package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Teacher;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface TeacherDao {

    @GET("teachers")
    Call<ApiResponse<List<Teacher>>> getTeachers(
            @Query("search") String search,
            @Query("specialty") String specialty,
            @Query("page") int page, @Query("limit") int limit);

    @GET("teachers/stats")
    Call<ApiResponse<Map<String, Object>>> getTeacherStats();

    @GET("teachers/{id}")
    Call<ApiResponse<Teacher>> getTeacher(@Path("id") String id);

    @POST("teachers")
    Call<ApiResponse<Teacher>> createTeacher(@Body Map<String, Object> data);

    @PUT("teachers/{id}")
    Call<ApiResponse<Teacher>> updateTeacher(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("teachers/{id}")
    Call<ApiResponse<Void>> deleteTeacher(@Path("id") String id);

    @POST("teachers/{id}/reactivate")
    Call<ApiResponse<Teacher>> reactivateTeacher(@Path("id") String id);
}