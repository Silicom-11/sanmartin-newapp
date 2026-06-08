package com.iepca.app.view.fragment.teacher;

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
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.DashboardDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.CourseAdapter;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherDashboardFragment extends Fragment {

    private TextView tvWelcome, tvTodaySummary;
    private RecyclerView rvCourses;
    private View btnTakeAttendance, btnAddGrades;
    private CourseDao courseDao;
    private DashboardDao dashboardDao;
    private CourseAdapter courseAdapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = SessionManager.getInstance(requireContext());
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        dashboardDao = RetrofitClient.createService(requireContext(), DashboardDao.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTodaySummary = view.findViewById(R.id.tvTodaySummary);
        rvCourses = view.findViewById(R.id.rvCourses);
        btnTakeAttendance = view.findViewById(R.id.btnTakeAttendance);
        btnAddGrades = view.findViewById(R.id.btnAddGrades);

        tvWelcome.setText("Bienvenido, " + session.getUserName());

        courseAdapter = new CourseAdapter(course -> {
            UIUtils.showToast(requireContext(), "Curso: " + course.getName());
        });
        rvCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCourses.setAdapter(courseAdapter);

        btnTakeAttendance.setOnClickListener(v -> navigateTo(new AttendanceFragment()));
        btnAddGrades.setOnClickListener(v -> navigateTo(new GradesFormFragment()));

        loadMyCourses();
        loadDashboard();
    }

    private void loadMyCourses() {
        courseDao.getMyCourses().enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    courseAdapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando cursos");
            }
        });
    }

    private void loadDashboard() {
        dashboardDao.getTeacherDashboard().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null) {
                        Object total = data.get("totalStudents");
                        int totalStudents = total instanceof Number ? ((Number) total).intValue() : 0;
                        tvTodaySummary.setText("Total de estudiantes: " + totalStudents);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof com.iepca.app.view.activity.MainActivity) {
            ((com.iepca.app.view.activity.MainActivity) getActivity()).loadFragment(fragment);
        }
    }
}
