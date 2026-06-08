package com.iepca.app.view.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.AuthDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.databinding.ActivityForgotPasswordBinding;
import com.iepca.app.util.UIUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSend.setOnClickListener(v -> sendReset());
        binding.tvBack.setOnClickListener(v -> finish());
    }

    private void sendReset() {
        String email = binding.etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            binding.tilEmail.setError("Ingrese su correo electrónico");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSend.setEnabled(false);

        AuthDao authDao = RetrofitClient.createService(this, AuthDao.class);
        Map<String, String> data = new HashMap<>();
        data.put("email", email);

        authDao.forgotPassword(data).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
                UIUtils.showToast(ForgotPasswordActivity.this,
                        "Se ha enviado un enlace de recuperación a su correo");
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);
                UIUtils.showErrorSnackbar(binding.getRoot(), "Error de conexión");
            }
        });
    }
}
