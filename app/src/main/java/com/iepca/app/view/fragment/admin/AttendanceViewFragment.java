package com.iepca.app.view.fragment.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.AttendanceDao;
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Attendance;
import com.iepca.app.model.Course;
import com.iepca.app.model.Student;
import com.iepca.app.model.enums.AttendanceStatus;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.AttendanceRecordAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin attendance monitor.
 *
 * Lets the administrator pick any course and date, shows the day summary
 * (present / absent / late) and the individual records with real student names.
 */
public class AttendanceViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnDate;
    private Spinner spinnerCourse;
    private TextView tvPresent, tvAbsent, tvLate, tvEmpty;

    private AttendanceDao attendanceDao;
    private CourseDao courseDao;
    private StudentDao studentDao;
    private AttendanceRecordAdapter adapter;

    private final Map<String, Student> studentDirectory = new HashMap<>();
    private List<Course> courses = new ArrayList<>();
    private String selectedCourseId;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attendanceDao = RetrofitClient.createService(requireContext(), AttendanceDao.class);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        btnDate = view.findViewById(R.id.btnDate);
        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        tvPresent = view.findViewById(R.id.tvPresent);
        tvAbsent = view.findViewById(R.id.tvAbsent);
        tvLate = view.findViewById(R.id.tvLate);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new AttendanceRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        btnDate.setText(selectedDate);
        btnDate.setOnClickListener(v -> showDatePicker());
        swipeRefresh.setOnRefreshListener(this::loadRecords);

        loadStudentDirectory();
    }

    private void loadStudentDirectory() {
        studentDao.getStudents(null, null, null, 1, 200).enqueue(
                new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Student> all = response.body().getData();
                    studentDirectory.clear();
                    if (all != null) {
                        for (Student s : all) {
                            if (s.getId() != null) studentDirectory.put(s.getId(), s);
                        }
                    }
                    adapter.setStudentDirectory(studentDirectory);
                }
                loadCourses();
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                loadCourses();
            }
        });
    }

    private void loadCourses() {
        courseDao.getCourses(null, null, null, 1, 100).enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    courses = response.body().getData();
                    if (courses == null) courses = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    for (Course c : courses) names.add(c.getName());
                    if (names.isEmpty()) names.add("Sin cursos registrados");
                    spinnerCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                    spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                            if (pos < courses.size()) {
                                selectedCourseId = courses.get(pos).getId();
                                loadRecords();
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> p) {}
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando cursos");
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            btnDate.setText(selectedDate);
            loadRecords();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadRecords() {
        if (selectedCourseId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }
        attendanceDao.getAttendanceByCourseDate(selectedCourseId, selectedDate).enqueue(
                new Callback<ApiResponse<List<Attendance>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Attendance>>> call,
                                   @NonNull Response<ApiResponse<List<Attendance>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                List<Attendance> records = response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() ? response.body().getData() : null;
                if (records == null) records = new ArrayList<>();
                adapter.setItems(records);
                updateSummary(records);
                tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Attendance>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                UIUtils.showToast(requireContext(), "Error cargando asistencia");
            }
        });
    }

    private void updateSummary(List<Attendance> records) {
        int present = 0, absent = 0, late = 0;
        for (Attendance a : records) {
            AttendanceStatus status = a.getStatus();
            if (status == AttendanceStatus.PRESENT) present++;
            else if (status == AttendanceStatus.LATE) late++;
            else if (status == AttendanceStatus.ABSENT) absent++;
        }
        tvPresent.setText("Presentes: " + present);
        tvAbsent.setText("Ausentes: " + absent);
        tvLate.setText("Tardanzas: " + late);
    }
}
