package com.iepca.app.view.fragment.admin;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.controller.StudentManagementController;
import com.iepca.app.dao.DashboardDao;
import com.iepca.app.dao.JustificationDao;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.DashboardStats;
import com.iepca.app.model.Justification;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.JustificationAdapter;
import com.iepca.app.view.adapter.StudentAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvWelcome, tvDate, tvStudentsCount, tvTeachersCount, tvParentsCount, tvCoursesCount;
    private PieChart chartAttendance;
    private RecyclerView rvPendingJustifications, rvRecentStudents;
    private TextView tvNoJustifications, tvNoStudents, btnSearchStudents;
    private MaterialButton btnNewStudent, btnNewTeacher;
    private MaterialCardView cardStudents, cardTeachers, cardParents, cardCourses;
    private View cardLiveMap;
    private SwipeRefreshLayout swipeRefresh;

    private DashboardDao dashboardDao;
    private JustificationDao justificationDao;
    private StudentManagementController studentController;
    private JustificationAdapter justificationAdapter;
    private StudentAdapter recentStudentsAdapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = SessionManager.getInstance(requireContext());
        dashboardDao = RetrofitClient.createService(requireContext(), DashboardDao.class);
        justificationDao = RetrofitClient.createService(requireContext(), JustificationDao.class);
        studentController = new StudentManagementController(requireContext());

        initViews(view);
        setupAdapters();
        setupListeners();
        refreshAll();
    }

    private void initViews(View v) {
        tvWelcome = v.findViewById(R.id.tvWelcome);
        tvDate = v.findViewById(R.id.tvDate);
        tvStudentsCount = v.findViewById(R.id.tvStudentsCount);
        tvTeachersCount = v.findViewById(R.id.tvTeachersCount);
        tvParentsCount = v.findViewById(R.id.tvParentsCount);
        tvCoursesCount = v.findViewById(R.id.tvCoursesCount);
        chartAttendance = v.findViewById(R.id.chartAttendance);
        rvPendingJustifications = v.findViewById(R.id.rvPendingJustifications);
        rvRecentStudents = v.findViewById(R.id.rvRecentStudents);
        tvNoJustifications = v.findViewById(R.id.tvNoJustifications);
        tvNoStudents = v.findViewById(R.id.tvNoStudents);
        btnSearchStudents = v.findViewById(R.id.btnSearchStudents);
        btnNewStudent = v.findViewById(R.id.btnNewStudent);
        btnNewTeacher = v.findViewById(R.id.btnNewTeacher);
        cardStudents = v.findViewById(R.id.cardStudents);
        cardTeachers = v.findViewById(R.id.cardTeachers);
        cardParents = v.findViewById(R.id.cardParents);
        cardCourses = v.findViewById(R.id.cardCourses);
        cardLiveMap = v.findViewById(R.id.cardLiveMap);
        swipeRefresh = v.findViewById(R.id.swipeRefresh);

        String dateStr = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("es")).format(new Date());
        tvDate.setText(dateStr);
        tvWelcome.setText("Bienvenido, " + session.getUserName());
    }

    private void setupAdapters() {
        justificationAdapter = new JustificationAdapter(new JustificationAdapter.OnActionListener() {
            @Override
            public void onApprove(Justification j) { reviewJustification(j.getId(), "approved"); }
            @Override
            public void onReject(Justification j) { reviewJustification(j.getId(), "rejected"); }
            @Override
            public void onItemClick(Justification j) {}
        }, true);
        rvPendingJustifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPendingJustifications.setAdapter(justificationAdapter);

        recentStudentsAdapter = new StudentAdapter(student -> navigateTo(new StudentsManagementFragment()));
        rvRecentStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentStudents.setAdapter(recentStudentsAdapter);
    }

    private void loadStudentNamesForJustifications() {
        studentController.listStudents("", 1, 200, new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> students) {
                if (!isAdded()) return;
                java.util.Map<String, String> names = new java.util.HashMap<>();
                if (students != null) {
                    for (Student s : students) {
                        if (s.getId() != null) names.put(s.getId(), s.getFullName());
                    }
                }
                justificationAdapter.setStudentNames(names);
            }
            @Override
            public void onError(String message) {}
        });
    }

    private void setupListeners() {
        btnNewStudent.setOnClickListener(v -> navigateTo(new StudentsManagementFragment()));
        btnNewTeacher.setOnClickListener(v -> navigateTo(new TeachersManagementFragment()));
        cardStudents.setOnClickListener(v -> navigateTo(new StudentsManagementFragment()));
        cardTeachers.setOnClickListener(v -> navigateTo(new TeachersManagementFragment()));
        cardParents.setOnClickListener(v -> navigateTo(new ParentsManagementFragment()));
        cardCourses.setOnClickListener(v -> navigateTo(new CoursesManagementFragment()));
        cardLiveMap.setOnClickListener(v -> navigateTo(new GPSTrackingFragment()));
        btnSearchStudents.setOnClickListener(v -> navigateTo(new StudentsManagementFragment()));
        swipeRefresh.setOnRefreshListener(this::refreshAll);
    }

    private void refreshAll() {
        loadDashboard();
        loadStudentNamesForJustifications();
        loadPendingJustifications();
        loadRecentStudents();
    }

    private void loadDashboard() {
        dashboardDao.getAdminDashboard().enqueue(new Callback<ApiResponse<DashboardStats>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<DashboardStats>> call,
                                   @NonNull Response<ApiResponse<DashboardStats>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DashboardStats stats = response.body().getData();
                    tvStudentsCount.setText(String.valueOf(stats.getTotalStudents()));
                    tvTeachersCount.setText(String.valueOf(stats.getTotalTeachers()));
                    tvParentsCount.setText(String.valueOf(stats.getTotalParents()));
                    tvCoursesCount.setText(String.valueOf(stats.getTotalCourses()));
                    setupAttendanceChart(stats.getAttendanceRate());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<DashboardStats>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    private void loadRecentStudents() {
        studentController.listStudents("", 1, 3, new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> students) {
                if (!isAdded()) return;
                recentStudentsAdapter.setItems(students);
                boolean empty = students == null || students.isEmpty();
                tvNoStudents.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvRecentStudents.setVisibility(empty ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                tvNoStudents.setVisibility(View.VISIBLE);
                rvRecentStudents.setVisibility(View.GONE);
            }
        });
    }

    private void setupAttendanceChart(double rate) {
        double normalizedRate = Math.max(0, Math.min(100, rate));
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) normalizedRate, "Asistencia"));
        entries.add(new PieEntry((float) (100 - normalizedRate), "Inasistencia"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(requireContext().getColor(R.color.success), requireContext().getColor(R.color.error));
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chartAttendance.setData(data);
        chartAttendance.setUsePercentValues(true);
        chartAttendance.getDescription().setEnabled(false);
        chartAttendance.setDrawEntryLabels(false);
        chartAttendance.setCenterText(String.format(Locale.getDefault(), "%.0f%%\nAsistencia", normalizedRate));
        chartAttendance.setCenterTextSize(16f);
        chartAttendance.setCenterTextColor(requireContext().getColor(R.color.text_primary));
        chartAttendance.setHoleRadius(55f);
        chartAttendance.setTransparentCircleRadius(60f);
        chartAttendance.getLegend().setWordWrapEnabled(true);
        chartAttendance.getLegend().setTextSize(12f);
        chartAttendance.animateY(800);
        chartAttendance.invalidate();
    }

    private void loadPendingJustifications() {
        justificationDao.getJustifications("pending", null, 1, 5).enqueue(
                new Callback<ApiResponse<List<Justification>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Justification>>> call,
                                   @NonNull Response<ApiResponse<List<Justification>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Justification> list = response.body().getData();
                    if (list != null && !list.isEmpty()) {
                        justificationAdapter.setItems(list);
                        tvNoJustifications.setVisibility(View.GONE);
                    } else {
                        justificationAdapter.setItems(new ArrayList<>());
                        tvNoJustifications.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Justification>>> call, @NonNull Throwable t) {}
        });
    }

    private void reviewJustification(String id, String status) {
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("status", status);
        justificationDao.reviewJustification(id, data).enqueue(new Callback<ApiResponse<Justification>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Justification>> call,
                                   @NonNull Response<ApiResponse<Justification>> response) {
                if (isAdded()) {
                    String label = "approved".equals(status) ? "aprobada" : "rechazada";
                    UIUtils.showToast(requireContext(), "Justificación " + label);
                    loadPendingJustifications();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Justification>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error al procesar");
            }
        });
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof com.iepca.app.view.activity.MainActivity) {
            ((com.iepca.app.view.activity.MainActivity) getActivity()).loadFragment(fragment);
        }
    }
}
