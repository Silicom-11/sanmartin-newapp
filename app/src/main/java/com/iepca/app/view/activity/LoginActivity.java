package com.iepca.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.iepca.app.config.SessionManager;
import com.iepca.app.controller.AuthController;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.databinding.ActivityLoginBinding;
import com.iepca.app.model.AuthResponse;
import com.iepca.app.model.User;
import com.iepca.app.util.UIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login screen.
 *
 * MVC role: View. It captures UI input and delegates the login workflow
 * to AuthController, which validates and calls AuthDao.
 */
public class LoginActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(LoginActivity.class);

    private ActivityLoginBinding binding;
    private AuthController authController;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authController = new AuthController(this);
        session = SessionManager.getInstance(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        setLoading(true);

        authController.login(email, password, new ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse auth) {
                setLoading(false);
                handleLoginSuccess(auth);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                UIUtils.showErrorSnackbar(binding.getRoot(), message);
                LOG.warn("Login failed: {}", message);
            }
        });
    }

    private void handleLoginSuccess(AuthResponse auth) {
        User user = auth.getUser();
        session.saveToken(auth.getToken());
        if (auth.getRefreshToken() != null) {
            session.saveRefreshToken(auth.getRefreshToken());
        }
        session.saveUserData(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getValue(),
                user.getAvatar() != null ? user.getAvatar() : ""
        );
        session.saveStudentProfile(user.getStudentProfile());

        LOG.info("Login successful: {} ({})", user.getEmail(), user.getRole());

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
    }
}
