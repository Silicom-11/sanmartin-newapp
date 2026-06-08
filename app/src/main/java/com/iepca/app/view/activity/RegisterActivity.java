package com.iepca.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.AuthDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.AuthResponse;
import com.iepca.app.model.User;
import com.iepca.app.databinding.ActivityRegisterBinding;
import com.iepca.app.util.UIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterActivity.class);
    private ActivityRegisterBinding binding;
    private AuthDao authDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authDao = RetrofitClient.createService(this, AuthDao.class);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()) {
            UIUtils.showToast(this, "Complete todos los campos");
            return;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        setLoading(true);

        Map<String, Object> data = new HashMap<>();
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        data.put("email", email);
        data.put("password", password);
        data.put("role", "padre"); // Default role for self-registration

        authDao.register(data).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                   Response<ApiResponse<AuthResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse auth = response.body().getData();
                    User user = auth.getUser();
                    SessionManager session = SessionManager.getInstance(RegisterActivity.this);
                    session.saveToken(auth.getToken());
                    session.saveUserData(user.getId(), user.getEmail(),
                            user.getFullName(), user.getRole().getValue(), "");

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Error al registrarse";
                    UIUtils.showErrorSnackbar(binding.getRoot(), msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                setLoading(false);
                UIUtils.showErrorSnackbar(binding.getRoot(), "Error de conexión");
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }
}
