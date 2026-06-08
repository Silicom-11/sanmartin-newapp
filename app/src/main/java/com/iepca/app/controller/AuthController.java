package com.iepca.app.controller;

import android.content.Context;

import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.AuthDao;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.AuthResponse;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication workflows.
 *
 * View layer calls this class; this class validates input and delegates
 * persistence/network access to AuthDao.
 */
public class AuthController extends BaseApiController {

    private final AuthDao authDao;

    public AuthController(Context context) {
        this(RetrofitClient.createService(context, AuthDao.class));
    }

    public AuthController(AuthDao authDao) {
        this.authDao = authDao;
    }

    public void login(String email, String password, ApiCallback<AuthResponse> callback) {
        if (StringUtils.isBlank(email)) {
            callback.onError("Ingrese su correo electronico");
            return;
        }
        if (StringUtils.isBlank(password)) {
            callback.onError("Ingrese su contrasena");
            return;
        }

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email.trim());
        credentials.put("password", password.trim());
        execute(authDao.login(credentials), callback);
    }
}
