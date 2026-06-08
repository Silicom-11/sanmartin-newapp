package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Notification;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface NotificationDao {

    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications(
            @Query("page") int page, @Query("limit") int limit);

    @GET("notifications/unread-count")
    Call<ApiResponse<Map<String, Integer>>> getUnreadCount();

    @PUT("notifications/{id}/read")
    Call<ApiResponse<Void>> markAsRead(@Path("id") String id);

    @PUT("notifications/read-all")
    Call<ApiResponse<Void>> markAllAsRead();

    @POST("notifications/broadcast")
    Call<ApiResponse<Void>> broadcast(@Body Map<String, Object> data);
}