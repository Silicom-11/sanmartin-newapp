package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.InstitutionDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Institution;
import com.iepca.app.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private Spinner spinnerYear;
    private TextInputEditText etInstitutionName, etInstitutionAddress;
    private MaterialButton btnSaveInstitution;
    private InstitutionDao institutionDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        institutionDao = RetrofitClient.createService(requireContext(), InstitutionDao.class);

        spinnerYear = view.findViewById(R.id.spinnerYear);
        etInstitutionName = view.findViewById(R.id.etInstitutionName);
        etInstitutionAddress = view.findViewById(R.id.etInstitutionAddress);
        btnSaveInstitution = view.findViewById(R.id.btnSaveInstitution);

        loadInstitution();
        loadAcademicYears();

        btnSaveInstitution.setOnClickListener(v -> saveInstitution());
    }

    private void loadInstitution() {
        institutionDao.getInstitution().enqueue(new Callback<ApiResponse<Institution>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Institution>> call,
                                   @NonNull Response<ApiResponse<Institution>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Institution inst = response.body().getData();
                    etInstitutionName.setText(inst.getName());
                    etInstitutionAddress.setText(inst.getAddress());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Institution>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando configuración");
            }
        });
    }

    private void loadAcademicYears() {
        institutionDao.getAcademicYears().enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, Object>> years = response.body().getData();
                    List<String> yearNames = new ArrayList<>();
                    for (Map<String, Object> y : years) {
                        yearNames.add(y.get("name") != null ? y.get("name").toString() : "Año");
                    }
                    if (yearNames.isEmpty()) yearNames.add("Sin años configurados");
                    spinnerYear.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, yearNames));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {}
        });
    }

    private void saveInstitution() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", etInstitutionName.getText().toString().trim());
        data.put("address", etInstitutionAddress.getText().toString().trim());

        institutionDao.updateInstitution(data).enqueue(new Callback<ApiResponse<Institution>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Institution>> call,
                                   @NonNull Response<ApiResponse<Institution>> response) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Configuración guardada");
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Institution>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error al guardar");
            }
        });
    }
}
