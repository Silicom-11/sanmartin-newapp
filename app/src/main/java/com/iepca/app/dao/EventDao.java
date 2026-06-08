package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Event;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface EventDao {

    @GET("events")
    Call<ApiResponse<List<Event>>> getEvents(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("type") String type);

    @GET("events/upcoming")
    Call<ApiResponse<List<Event>>> getUpcomingEvents();

    @GET("events/{id}")
    Call<ApiResponse<Event>> getEvent(@Path("id") String id);

    @POST("events")
    Call<ApiResponse<Event>> createEvent(@Body Map<String, Object> data);

    @PUT("events/{id}")
    Call<ApiResponse<Event>> updateEvent(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("events/{id}")
    Call<ApiResponse<Void>> deleteEvent(@Path("id") String id);
}