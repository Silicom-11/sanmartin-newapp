package com.iepca.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.iepca.app.R;
import com.iepca.app.config.App;
import com.iepca.app.config.Constants;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.LocationDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;
import com.iepca.app.view.activity.MainActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Foreground service for GPS location tracking.
 * Sends student location to backend every 30 seconds.
 * Implements Service pattern (Android component lifecycle).
 */
public class LocationTrackingService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(LocationTrackingService.class);
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private LocationDao locationDao;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        locationDao = RetrofitClient.createService(this, LocationDao.class);
        LOG.info("LocationTrackingService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Constants.LOCATION_NOTIFICATION_ID, buildNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (locationCallback != null) {
            return;
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, Constants.LOCATION_INTERVAL_MS)
                .setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL_MS)
                .setMinUpdateDistanceMeters(Constants.LOCATION_MIN_DISTANCE_M)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    android.location.Location loc = locationResult.getLastLocation();
                    sendLocationToServer(loc);
                }
            }
        };

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            LOG.info("Location updates started");
        } catch (SecurityException e) {
            LOG.error("Location permission denied", e);
        }
    }

    private void sendLocationToServer(android.location.Location loc) {
        Map<String, Object> coordinates = new HashMap<>();
        coordinates.put("latitude", loc.getLatitude());
        coordinates.put("longitude", loc.getLongitude());
        coordinates.put("accuracy", loc.getAccuracy());
        coordinates.put("altitude", loc.hasAltitude() ? loc.getAltitude() : null);
        coordinates.put("speed", loc.hasSpeed() ? loc.getSpeed() : 0);
        coordinates.put("heading", loc.hasBearing() ? loc.getBearing() : 0);

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", loc.getLatitude());
        data.put("longitude", loc.getLongitude());
        data.put("accuracy", loc.getAccuracy());
        data.put("altitude", loc.hasAltitude() ? loc.getAltitude() : null);
        data.put("speed", loc.hasSpeed() ? loc.getSpeed() : 0);
        data.put("heading", loc.hasBearing() ? loc.getBearing() : 0);
        data.put("coordinates", coordinates);
        data.put("battery", getBatteryLevel());
        data.put("networkType", getNetworkType());
        data.put("platform", "android");
        data.put("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        data.put("appVersion", "1.0.0");
        data.put("sessionStatus", "online");
        data.put("updateType", "periodic");

        locationDao.sendLocation(data).enqueue(new Callback<ApiResponse<Location>>() {
            @Override
            public void onResponse(Call<ApiResponse<Location>> call, Response<ApiResponse<Location>> r) {
                if (r.isSuccessful()) {
                    LOG.debug("Location sent: {},{}", loc.getLatitude(), loc.getLongitude());
                } else {
                    LOG.warn("Location rejected by server. HTTP {}", r.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Location>> call, Throwable t) {
                LOG.warn("Failed to send location: {}", t.getMessage());
            }
        });
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, App.CHANNEL_LOCATION)
                .setContentTitle("Seguimiento GPS activo")
                .setContentText("Tu ubicación está siendo compartida")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private int getBatteryLevel() {
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        return batteryManager != null
                ? batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                : 0;
    }

    private String getNetworkType() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return "unknown";

        Network network = manager.getActiveNetwork();
        if (network == null) return "none";

        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null) return "unknown";

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "wifi";
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "mobile";
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "ethernet";
        return "unknown";
    }

    @Override
    public void onDestroy() {
        if (fusedClient != null && locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
        LOG.info("LocationTrackingService destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
