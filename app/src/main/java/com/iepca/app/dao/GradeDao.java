package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Evaluation;
import com.iepca.app.model.Grade;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface GradeDao {

    @GET("grades/course/{courseId}")
    Call<ApiResponse<List<Grade>>> getGradesByCourse(
            @Path("courseId") String courseId,
            @Query("bimester") int bimester);

    @GET("grades/history/{studentId}")
    Call<ApiResponse<List<Grade>>> getGradeHistory(@Path("studentId") String studentId);

    @GET("grades/stats")
    Call<ApiResponse<Map<String, Object>>> getGradeStats(
            @Query("courseId") String courseId,
            @Query("bimester") int bimester);

    @GET("grades/report/{courseId}")
    Call<ApiResponse<Map<String, Object>>> getCourseReport(@Path("courseId") String courseId);

    @POST("grades/save-score")
    Call<ApiResponse<Grade>> saveScore(@Body Map<String, Object> data);

    @POST("grades/save-bulk")
    Call<ApiResponse<List<Grade>>> saveBulk(@Body Map<String, Object> data);

    @PUT("grades/close-bimester")
    Call<ApiResponse<Void>> closeBimester(@Body Map<String, Object> data);

    @PUT("grades/reopen-bimester")
    Call<ApiResponse<Void>> reopenBimester(@Body Map<String, Object> data);

    @PUT("grades/publish-bimester")
    Call<ApiResponse<Void>> publishBimester(@Body Map<String, Object> data);

    // Evaluations
    @GET("evaluations/course/{courseId}")
    Call<ApiResponse<List<Evaluation>>> getEvaluations(
            @Path("courseId") String courseId,
            @Query("bimester") int bimester);

    @POST("evaluations")
    Call<ApiResponse<Evaluation>> createEvaluation(@Body Map<String, Object> data);

    @PUT("evaluations/{id}")
    Call<ApiResponse<Evaluation>> updateEvaluation(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("evaluations/{id}")
    Call<ApiResponse<Void>> deleteEvaluation(@Path("id") String id);
}