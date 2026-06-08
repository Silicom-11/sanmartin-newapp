package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Institution;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface InstitutionDao {

    @GET("institution")
    Call<ApiResponse<Institution>> getInstitution();

    @PUT("institution")
    Call<ApiResponse<Institution>> updateInstitution(@Body Map<String, Object> data);

    @GET("institution/academic-years")
    Call<ApiResponse<List<Map<String, Object>>>> getAcademicYears();

    @GET("institution/academic-years/current")
    Call<ApiResponse<Map<String, Object>>> getCurrentAcademicYear();

    @GET("institution/grade-levels")
    Call<ApiResponse<List<Map<String, Object>>>> getGradeLevels();

    @GET("institution/subjects")
    Call<ApiResponse<List<Map<String, Object>>>> getSubjects();

    @POST("institution/subjects")
    Call<ApiResponse<Map<String, Object>>> createSubject(@Body Map<String, Object> data);
}