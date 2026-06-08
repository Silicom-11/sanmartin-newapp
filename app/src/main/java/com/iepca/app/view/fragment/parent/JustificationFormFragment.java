package com.iepca.app.view.fragment.parent;

import android.app.DatePickerDialog;
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
import com.iepca.app.dao.JustificationDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Justification;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;

import java.util.*;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JustificationFormFragment extends Fragment {

    private Spinner spinnerChild, spinnerReason;
    private MaterialButton btnDateRange, btnAttachEvidence, btnSubmit;
    private TextInputEditText etDescription;
    private StudentDao studentDao;
    private JustificationDao justificationDao;
    private List<Student> children = new ArrayList<>();
    private String startDate = "", endDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_justification_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);
        justificationDao = RetrofitClient.createService(requireContext(), JustificationDao.class);

        spinnerChild = view.findViewById(R.id.spinnerChild);
        spinnerReason = view.findViewById(R.id.spinnerReason);
        btnDateRange = view.findViewById(R.id.btnDateRange);
        btnAttachEvidence = view.findViewById(R.id.btnAttachEvidence);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        etDescription = view.findViewById(R.id.etDescription);

        String[] reasons = {"Enfermedad", "Cita médica", "Emergencia familiar", "Viaje", "Otro"};
        spinnerReason.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, reasons));

        btnDateRange.setOnClickListener(v -> showDatePicker());
        btnAttachEvidence.setOnClickListener(v ->
                UIUtils.showToast(requireContext(), "Seleccionar archivo..."));
        btnSubmit.setOnClickListener(v -> submitJustification());

        loadChildren();
    }

    private void loadChildren() {
        studentDao.getMyChildren().enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    children = response.body().getData();
                    List<String> names = new ArrayList<>();
                    for (Student s : children) names.add(s.getFullName());
                    spinnerChild.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando hijos");
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (v, year, month, day) -> {
            startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            endDate = startDate;
            btnDateRange.setText(startDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submitJustification() {
        int childPos = spinnerChild.getSelectedItemPosition();
        if (childPos < 0 || children.isEmpty()) {
            UIUtils.showToast(requireContext(), "Seleccione un hijo");
            return;
        }
        if (startDate.isEmpty()) {
            UIUtils.showToast(requireContext(), "Seleccione fecha");
            return;
        }

        String studentId = children.get(childPos).getId();
        String reason = spinnerReason.getSelectedItem().toString();
        String description = etDescription.getText() != null ? etDescription.getText().toString() : "";
        String dates = startDate;

        MediaType textType = MediaType.parse("text/plain");
        justificationDao.submitJustification(
                RequestBody.create(textType, studentId),
                RequestBody.create(textType, reason),
                RequestBody.create(textType, dates),
                RequestBody.create(textType, description),
                new ArrayList<>()
        ).enqueue(new Callback<ApiResponse<Justification>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Justification>> call,
                                   @NonNull Response<ApiResponse<Justification>> response) {
                if (isAdded()) {
                    UIUtils.showToast(requireContext(), "Justificación enviada");
                    if (getActivity() != null) getActivity().onBackPressed();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Justification>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error al enviar justificación");
            }
        });
    }
}
