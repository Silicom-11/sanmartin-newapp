package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.DashboardStats;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface DashboardDao {

    @GET("dashboard/admin")
    Call<ApiResponse<DashboardStats>> getAdminDashboard();

    @GET("dashboard/teacher")
    Call<ApiResponse<Map<String, Object>>> getTeacherDashboard();

    @GET("dashboard/parent")
    Call<ApiResponse<Map<String, Object>>> getParentDashboard();
}