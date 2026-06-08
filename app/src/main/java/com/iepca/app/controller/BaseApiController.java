package com.iepca.app.controller;

import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base controller for Retrofit use cases.
 *
 * MVC role: Controller.
 * SOLID:
 * - Single Responsibility: centralizes API response handling.
 * - Open/Closed: feature controllers reuse this behavior without duplicating it.
 * - Dependency Inversion: callers depend on ApiCallback, not on Android views.
 */
public abstract class BaseApiController {

    private static final Logger LOG = LoggerFactory.getLogger(BaseApiController.class);

    protected <T> void execute(Call<ApiResponse<T>> call, ApiCallback<T> callback) {
        if (call == null) {
            callback.onError("Operacion no disponible");
            return;
        }

        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                ApiResponse<T> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    callback.onSuccess(body.getData());
                    return;
                }

                String message = "No se pudo completar la operacion";
                if (body != null && body.getMessage() != null) {
                    message = body.getMessage();
                }
                LOG.warn("API response rejected: HTTP {} - {}", response.code(), message);
                callback.onError(message);
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable throwable) {
                LOG.error("Network/API failure", throwable);
                callback.onError("Error de conexion: " + throwable.getMessage());
            }
        });
    }
}
