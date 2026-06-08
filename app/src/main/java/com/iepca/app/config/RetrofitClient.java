package com.iepca.app.config;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit HTTP client configuration.
 * Singleton pattern with JWT token injection interceptor.
 * Follows SOLID: Single Responsibility - only HTTP client setup.
 */
public class RetrofitClient {

    private static final Logger LOG = LoggerFactory.getLogger(RetrofitClient.class);
    private static Retrofit retrofit;

    private RetrofitClient() {
        throw new UnsupportedOperationException("Use getClient()");
    }

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    message -> LOG.debug("HTTP: {}", message)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            SessionManager session = SessionManager.getInstance(context);

            Interceptor authInterceptor = chain -> {
                Request.Builder builder = chain.request().newBuilder();
                String token = session.getToken();
                if (token != null && !token.isEmpty()) {
                    builder.addHeader("Authorization", "Bearer " + token);
                }
                builder.addHeader("Content-Type", "application/json");
                builder.addHeader("Accept", "application/json");
                return chain.proceed(builder.build());
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            LOG.info("Retrofit client initialized: {}", Constants.BASE_URL);
        }
        return retrofit;
    }

    public static <T> T createService(Context context, Class<T> serviceClass) {
        return getClient(context).create(serviceClass);
    }

    public static void resetClient() {
        retrofit = null;
        LOG.info("Retrofit client reset");
    }
}
