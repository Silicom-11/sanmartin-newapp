package com.iepca.app.service;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.AuthDao;
import com.iepca.app.model.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Firebase Cloud Messaging service for push notifications.
 */
public class FCMService extends FirebaseMessagingService {

    private static final Logger LOG = LoggerFactory.getLogger(FCMService.class);

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        LOG.info("New FCM token received and stored securely");
        SessionManager session = SessionManager.getInstance(this);
        session.saveFcmToken(token);

        if (session.isLoggedIn()) {
            registerTokenWithServer(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        LOG.info("FCM message received with {} data entries", message.getData().size());
        // TODO: Show notification
    }

    private void registerTokenWithServer(String token) {
        AuthDao authDao = RetrofitClient.createService(this, AuthDao.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("platform", "android");

        authDao.registerPushToken(data).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> r) {
                LOG.info("FCM token registered with server");
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                LOG.warn("Failed to register FCM token: {}", t.getMessage());
            }
        });
    }
}
