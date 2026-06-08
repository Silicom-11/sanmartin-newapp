package com.iepca.app.view.fragment.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.AttendanceDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Grade;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardFragment extends Fragment {

    private TextView tvWelcome, tvAttendancePercent, tvAttendanceDetail;
    private RecyclerView rvGrades;
    private GradeSummaryAdapter gradeAdapter;
    private SessionManager session;
    private AttendanceDao attendanceDao;
    private GradeDao gradeDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = SessionManager.getInstance(requireContext());
        attendanceDao = RetrofitClient.createService(requireContext(), AttendanceDao.class);
        gradeDao = RetrofitClient.createService(requireContext(), GradeDao.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvAttendanceDetail = view.findViewById(R.id.tvAttendanceDetail);
        rvGrades = view.findViewById(R.id.rvGrades);

        tvWelcome.setText("Hola, " + session.getUserName());

        gradeAdapter = new GradeSummaryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(gradeAdapter);

        loadAttendanceStats();
        loadGrades();
    }

    private void loadAttendanceStats() {
        String studentId = session.getStudentProfileId().isEmpty()
                ? session.getUserId()
                : session.getStudentProfileId();
        attendanceDao.getStudentAttendanceStats(studentId).enqueue(
                new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null) {
                        Object total = data.get("total");
                        Object present = data.get("present");
                        double totalValue = total instanceof Number ? ((Number) total).doubleValue() : 0.0;
                        double presentValue = present instanceof Number ? ((Number) present).doubleValue() : 0.0;
                        double rate = totalValue > 0 ? (presentValue / totalValue) * 100.0 : 0.0;
                        tvAttendancePercent.setText(String.format(java.util.Locale.getDefault(), "%.0f%%", rate));
                        tvAttendanceDetail.setText("de asistencia registrada");
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadGrades() {
        String studentId = session.getStudentProfileId().isEmpty()
                ? session.getUserId()
                : session.getStudentProfileId();
        gradeDao.getGradeHistory(studentId).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    gradeAdapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }
}
