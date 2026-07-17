package com.iepca.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.AuthDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.view.activity.SplashActivity;

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
    private static final String CHANNEL_ID = "iepca_general";

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

        String title = message.getNotification() != null && message.getNotification().getTitle() != null
                ? message.getNotification().getTitle()
                : message.getData().getOrDefault("title", "IEP Continental Americano");
        String body = message.getNotification() != null && message.getNotification().getBody() != null
                ? message.getNotification().getBody()
                : message.getData().getOrDefault("message", "");

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Notificaciones IEPCA", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Comunicados y avisos institucionales");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
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
