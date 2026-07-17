package com.iepca.app.config;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application class for IEP Continental Americano.
 * Initializes global configuration, notification channels, and logging.
 * Follows SOLID: Single Responsibility - only app-level initialization.
 */
public class App extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);
    private static App instance;

    public static final String CHANNEL_GENERAL = "iepca_general";
    public static final String CHANNEL_LOCATION = "iepca_location";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LOG.info("Application started - IEP Continental Americano");
        createNotificationChannels();
        configureMapEngine();
    }

    /**
     * osmdroid (OpenStreetMap) setup: identify the app to the tile servers
     * and keep the tile cache inside app-private storage.
     */
    private void configureMapEngine() {
        org.osmdroid.config.IConfigurationProvider config =
                org.osmdroid.config.Configuration.getInstance();
        config.load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE));
        config.setUserAgentValue(getPackageName());
        config.setOsmdroidBasePath(new java.io.File(getFilesDir(), "osmdroid"));
        config.setOsmdroidTileCache(new java.io.File(getFilesDir(), "osmdroid/tiles"));
        LOG.info("Map engine configured (osmdroid)");
    }

    public static App getInstance() {
        return instance;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            NotificationChannel general = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "Notificaciones Generales",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            general.setDescription("Notificaciones de notas, asistencia, mensajes y eventos");
            manager.createNotificationChannel(general);

            NotificationChannel location = new NotificationChannel(
                    CHANNEL_LOCATION,
                    "Seguimiento GPS",
                    NotificationManager.IMPORTANCE_LOW
            );
            location.setDescription("Seguimiento de ubicación en segundo plano");
            manager.createNotificationChannel(location);

            LOG.info("Notification channels created");
        }
    }
}
