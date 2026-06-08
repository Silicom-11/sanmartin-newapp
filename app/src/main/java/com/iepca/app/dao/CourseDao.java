package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface CourseDao {

    @GET("courses")
    Call<ApiResponse<List<Course>>> getCourses(
            @Query("gradeLevel") String gradeLevel,
            @Query("section") String section,
            @Query("teacherId") String teacherId,
            @Query("page") int page, @Query("limit") int limit);

    @GET("courses/stats")
    Call<ApiResponse<Map<String, Object>>> getCourseStats();

    @GET("courses/my-courses")
    Call<ApiResponse<List<Course>>> getMyCourses();

    @GET("courses/{id}")
    Call<ApiResponse<Course>> getCourse(@Path("id") String id);

    @POST("courses")
    Call<ApiResponse<Course>> createCourse(@Body Map<String, Object> data);

    @PUT("courses/{id}")
    Call<ApiResponse<Course>> updateCourse(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("courses/{id}")
    Call<ApiResponse<Void>> deleteCourse(@Path("id") String id);

    @POST("courses/{id}/students")
    Call<ApiResponse<Course>> addStudents(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("courses/{id}/students/{studentId}")
    Call<ApiResponse<Course>> removeStudent(@Path("id") String id, @Path("studentId") String studentId);
}