package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Student;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface StudentDao {

    @GET("students")
    Call<ApiResponse<List<Student>>> getStudents(
            @Query("gradeLevel") String gradeLevel,
            @Query("section") String section,
            @Query("search") String search,
            @Query("page") int page,
            @Query("limit") int limit);

    @GET("students/{id}")
    Call<ApiResponse<Student>> getStudent(@Path("id") String id);

    @POST("students")
    Call<ApiResponse<Student>> createStudent(@Body Map<String, Object> data);

    @PUT("students/{id}")
    Call<ApiResponse<Student>> updateStudent(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("students/{id}")
    Call<ApiResponse<Void>> deleteStudent(@Path("id") String id);

    @GET("students/my-children")
    Call<ApiResponse<List<Student>>> getMyChildren();

    // Admin management
    @GET("students-management")
    Call<ApiResponse<List<Student>>> getStudentsManagement(
            @Query("page") int page, @Query("limit") int limit,
            @Query("search") String search, @Query("gradeLevel") String grade);

    @GET("students-management/stats")
    Call<ApiResponse<Map<String, Object>>> getStudentStats();

    @POST("students-management/{id}/reactivate")
    Call<ApiResponse<Student>> reactivateStudent(@Path("id") String id);
}