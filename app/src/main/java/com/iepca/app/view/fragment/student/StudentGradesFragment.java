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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.AttendanceDao;
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Grade;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Student grades screen.
 *
 * Covers RF05 (notas por curso), RF06 (promedio general) and
 * RF17/RF18 (semáforo de rendimiento verde/amarillo/rojo).
 */
public class StudentGradesFragment extends Fragment {

    private TextView tvAverage, tvGradeLetter, tvCoursesCount, tvSemaforo, tvSemaforoDetail, tvEmpty;
    private RecyclerView rvGrades;
    private SwipeRefreshLayout swipeRefresh;
    private GradeDao gradeDao;
    private AttendanceDao attendanceDao;
    private CourseDao courseDao;
    private GradeSummaryAdapter adapter;
    private SessionManager session;

    private double generalAverage = 0.0;
    private double attendanceRate = -1.0;
    private boolean hasGrades = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = SessionManager.getInstance(requireContext());
        gradeDao = RetrofitClient.createService(requireContext(), GradeDao.class);
        attendanceDao = RetrofitClient.createService(requireContext(), AttendanceDao.class);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);

        tvAverage = view.findViewById(R.id.tvAverage);
        tvGradeLetter = view.findViewById(R.id.tvGradeLetter);
        tvCoursesCount = view.findViewById(R.id.tvCoursesCount);
        tvSemaforo = view.findViewById(R.id.tvSemaforo);
        tvSemaforoDetail = view.findViewById(R.id.tvSemaforoDetail);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvGrades = view.findViewById(R.id.rvGrades);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        adapter = new GradeSummaryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::refreshAll);
        refreshAll();
    }

    private String resolveStudentId() {
        return session.getStudentProfileId().isEmpty()
                ? session.getUserId()
                : session.getStudentProfileId();
    }

    private void refreshAll() {
        loadCourseNames();
        loadGrades();
        loadAttendanceRate();
    }

    private void loadCourseNames() {
        courseDao.getCourses(null, null, null, 1, 100).enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Course> courses = response.body().getData();
                    Map<String, String> names = new HashMap<>();
                    if (courses != null) {
                        for (Course c : courses) {
                            if (c.getId() != null) names.put(c.getId(), c.getName());
                        }
                    }
                    adapter.setCourseNames(names);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadGrades() {
        gradeDao.getGradeHistory(resolveStudentId()).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Grade> grades = response.body().getData();
                    adapter.setItems(grades);

                    boolean empty = grades == null || grades.isEmpty();
                    hasGrades = !empty;
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rvGrades.setVisibility(empty ? View.GONE : View.VISIBLE);

                    if (!empty) {
                        double sum = 0;
                        int count = 0;
                        java.util.Set<String> courses = new java.util.HashSet<>();
                        for (Grade g : grades) {
                            double value = g.getScore();
                            if (value > 0) {
                                sum += value;
                                count++;
                            }
                            if (g.getCourseId() != null) courses.add(g.getCourseId());
                        }
                        generalAverage = count > 0 ? sum / count : 0;
                        tvAverage.setText(String.format(Locale.getDefault(), "%.1f", generalAverage));
                        tvGradeLetter.setText(letterFor(generalAverage));
                        tvCoursesCount.setText(courses.size() == 1
                                ? "1 curso" : courses.size() + " cursos");
                    } else {
                        generalAverage = 0;
                        tvAverage.setText("--");
                        tvGradeLetter.setText("--");
                        tvCoursesCount.setText("0 cursos");
                    }
                    updateSemaforo();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }

    private void loadAttendanceRate() {
        attendanceDao.getStudentAttendanceStats(resolveStudentId()).enqueue(
                new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    double total = num(data, "total");
                    double present = num(data, "present");
                    double justified = num(data, "justified");
                    attendanceRate = total > 0 ? ((present + justified) / total) * 100.0 : -1;
                    updateSemaforo();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    private double num(Map<String, Object> data, String key) {
        if (data == null) return 0;
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }

    /** RF17/RF18: performance semaphore with green/yellow/red indicator. */
    private void updateSemaforo() {
        int bg;
        String label;
        String detail;

        double rate = attendanceRate < 0 ? 100 : attendanceRate;

        if (!hasGrades && attendanceRate < 0) {
            bg = requireContext().getColor(R.color.primary);
            label = "Semáforo: sin datos";
            detail = "El indicador se activa cuando existan notas y asistencias registradas.";
        } else if (generalAverage >= 14 && rate >= 90) {
            bg = requireContext().getColor(R.color.success);
            label = "Semáforo: VERDE";
            detail = "¡Buen trabajo! Mantienes buen promedio y asistencia.";
        } else if (generalAverage >= 11 && rate >= 75) {
            bg = requireContext().getColor(R.color.warning);
            label = "Semáforo: AMARILLO";
            detail = "Atención: refuerza tus cursos y evita tardanzas.";
        } else {
            bg = requireContext().getColor(R.color.error);
            label = "Semáforo: ROJO";
            detail = "Riesgo académico: conversa con tu tutor y organiza un plan de estudio.";
        }

        tvSemaforo.setText(label);
        tvSemaforo.getBackground().mutate().setTint(bg);
        tvSemaforoDetail.setText(detail);
    }

    private String letterFor(double value) {
        if (value >= 17) return "AD";
        if (value >= 14) return "A";
        if (value >= 11) return "B";
        return "C";
    }
}
