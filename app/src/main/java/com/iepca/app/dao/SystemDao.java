package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SystemDao {

    @GET("system/health-details")
    Call<ApiResponse<Map<String, Object>>> getHealthDetails();

    @GET("system/resources-status")
    Call<ApiResponse<Map<String, Object>>> getResourcesStatus();
}
