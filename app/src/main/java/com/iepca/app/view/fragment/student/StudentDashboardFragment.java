package com.iepca.app.view.fragment.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.iepca.app.dao.EventDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Event;
import com.iepca.app.model.Grade;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;
import com.iepca.app.view.fragment.shared.NotificationsFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentDashboardFragment extends Fragment {

    private TextView tvWelcome, tvAttendancePercent, tvAttendanceDetail;
    private TextView tvPresentCount, tvLateCount, tvNoGrades, tvNoEvents, btnViewAllGrades;
    private LinearLayout containerEvents;
    private View btnNotifications;
    private RecyclerView rvGrades;
    private SwipeRefreshLayout swipeRefresh;
    private GradeSummaryAdapter gradeAdapter;
    private SessionManager session;
    private AttendanceDao attendanceDao;
    private GradeDao gradeDao;
    private EventDao eventDao;
    private CourseDao courseDao;

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
        eventDao = RetrofitClient.createService(requireContext(), EventDao.class);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvAttendanceDetail = view.findViewById(R.id.tvAttendanceDetail);
        tvPresentCount = view.findViewById(R.id.tvPresentCount);
        tvLateCount = view.findViewById(R.id.tvLateCount);
        tvNoGrades = view.findViewById(R.id.tvNoGrades);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        btnViewAllGrades = view.findViewById(R.id.btnViewAllGrades);
        containerEvents = view.findViewById(R.id.containerEvents);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        rvGrades = view.findViewById(R.id.rvGrades);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        tvWelcome.setText("Hola, " + session.getUserName());

        gradeAdapter = new GradeSummaryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(gradeAdapter);

        btnViewAllGrades.setOnClickListener(v -> navigateTo(new StudentGradesFragment()));
        btnNotifications.setOnClickListener(v -> navigateTo(new NotificationsFragment()));
        swipeRefresh.setOnRefreshListener(this::refreshAll);

        refreshAll();
    }

    private void refreshAll() {
        loadCourseNames();
        loadAttendanceStats();
        loadGrades();
        loadUpcomingEvents();
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
                    gradeAdapter.setCourseNames(names);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {}
        });
    }

    private String resolveStudentId() {
        return session.getStudentProfileId().isEmpty()
                ? session.getUserId()
                : session.getStudentProfileId();
    }

    private void loadAttendanceStats() {
        attendanceDao.getStudentAttendanceStats(resolveStudentId()).enqueue(
                new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null) {
                        double total = asDouble(data.get("total"));
                        double present = asDouble(data.get("present"));
                        double late = asDouble(data.get("late"));
                        double rate = total > 0 ? (present / total) * 100.0 : 0.0;
                        tvAttendancePercent.setText(String.format(Locale.getDefault(), "%.0f%%", rate));
                        tvAttendanceDetail.setText(total > 0
                                ? "de asistencia registrada (" + (int) total + " registros)"
                                : "sin registros de asistencia todavía");
                        tvPresentCount.setText("Presentes: " + (int) present);
                        tvLateCount.setText("Tardanzas: " + (int) late);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private double asDouble(Object value) {
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }

    private void loadGrades() {
        gradeDao.getGradeHistory(resolveStudentId()).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Grade> grades = response.body().getData();
                    gradeAdapter.setItems(grades);
                    boolean empty = grades == null || grades.isEmpty();
                    tvNoGrades.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rvGrades.setVisibility(empty ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }

    private void loadUpcomingEvents() {
        eventDao.getUpcomingEvents().enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Event>>> call,
                                   @NonNull Response<ApiResponse<List<Event>>> response) {
                if (!isAdded()) return;
                containerEvents.removeAllViews();
                List<Event> events = response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() ? response.body().getData() : null;
                if (events == null || events.isEmpty()) {
                    tvNoEvents.setVisibility(View.VISIBLE);
                    return;
                }
                tvNoEvents.setVisibility(View.GONE);
                int max = Math.min(events.size(), 3);
                for (int i = 0; i < max; i++) {
                    Event event = events.get(i);
                    TextView tv = new TextView(requireContext());
                    String date = event.getDate() != null && event.getDate().length() >= 10
                            ? event.getDate().substring(0, 10) : "";
                    tv.setText(event.getTitle() + "\n" + date
                            + (event.getLocation() != null && !event.getLocation().isEmpty()
                            ? " — " + event.getLocation() : ""));
                    tv.setTextColor(requireContext().getColor(R.color.text_secondary));
                    tv.setTextSize(14f);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = (int) (10 * getResources().getDisplayMetrics().density);
                    tv.setLayoutParams(params);
                    containerEvents.addView(tv);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvNoEvents.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof com.iepca.app.view.activity.MainActivity) {
            ((com.iepca.app.view.activity.MainActivity) getActivity()).loadFragment(fragment);
        }
    }
}
