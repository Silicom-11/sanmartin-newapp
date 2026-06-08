package com.iepca.app.dao;

import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Conversation;
import com.iepca.app.model.Message;
import com.iepca.app.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface MessageDao {

    @GET("messages/conversations")
    Call<ApiResponse<List<Conversation>>> getConversations();

    @GET("messages/conversations/{id}")
    Call<ApiResponse<List<Message>>> getMessages(@Path("id") String conversationId);

    @POST("messages/send")
    Call<ApiResponse<Message>> sendMessage(@Body Map<String, String> data);

    @GET("messages/contacts")
    Call<ApiResponse<List<User>>> getContacts();

    @GET("messages/unread-count")
    Call<ApiResponse<Map<String, Integer>>> getUnreadCount();

    @PUT("messages/conversations/{id}/read")
    Call<ApiResponse<Void>> markConversationRead(@Path("id") String conversationId);
}