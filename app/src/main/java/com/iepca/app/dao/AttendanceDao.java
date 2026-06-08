package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Attendance;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface AttendanceDao {

    @GET("attendance/course/{courseId}/date/{date}")
    Call<ApiResponse<List<Attendance>>> getAttendanceByCourseDate(
            @Path("courseId") String courseId,
            @Path("date") String date);

    @GET("attendance/stats")
    Call<ApiResponse<Map<String, Object>>> getAttendanceStats();

    @GET("attendance/stats/{studentId}")
    Call<ApiResponse<Map<String, Object>>> getStudentAttendanceStats(@Path("studentId") String studentId);

    @POST("attendance/bulk")
    Call<ApiResponse<List<Attendance>>> saveBulkAttendance(@Body Map<String, Object> data);
}