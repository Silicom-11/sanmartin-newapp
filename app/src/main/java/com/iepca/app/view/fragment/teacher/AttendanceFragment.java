package com.iepca.app.view.fragment.teacher;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.AttendanceAdapter;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceFragment extends Fragment {

    private Spinner spinnerCourse;
    private MaterialButton btnDate, btnSave;
    private RecyclerView rvStudents;
    private android.widget.TextView tvTotal, tvPresentCount;

    private CourseDao courseDao;
    private AttendanceDao attendanceDao;
    private StudentDao studentDao;
    private AttendanceAdapter adapter;
    private List<Course> courses = new ArrayList<>();
    private final Map<String, Student> studentDirectory = new HashMap<>();
    private String selectedDate;
    private String selectedCourseId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        attendanceDao = RetrofitClient.createService(requireContext(), AttendanceDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        btnDate = view.findViewById(R.id.btnDate);
        btnSave = view.findViewById(R.id.btnSave);
        rvStudents = view.findViewById(R.id.rvStudents);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvPresentCount = view.findViewById(R.id.tvPresentCount);

        adapter = new AttendanceAdapter();
        adapter.setOnCountChangeListener((total, present) -> {
            tvTotal.setText("TOTAL\n" + total);
            tvPresentCount.setText("PRESENTES\n" + present);
        });
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(adapter);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        btnDate.setText(selectedDate);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveAttendance());

        loadStudentDirectory();
    }

    /** Loads the real list of students so course rosters show real names. */
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
                }
                loadMyCourses();
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                loadMyCourses();
            }
        });
    }

    private void loadMyCourses() {
        courseDao.getMyCourses().enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    courses = response.body().getData();
                    List<String> names = new ArrayList<>();
                    for (Course c : courses) names.add(c.getName());
                    spinnerCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                    spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                            selectedCourseId = courses.get(pos).getId();
                            adapter.setStudents(buildStudentsFromSelectedCourse());
                            loadAttendance();
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

    private void loadAttendance() {
        if (selectedCourseId == null) return;
        attendanceDao.getAttendanceByCourseDate(selectedCourseId, selectedDate).enqueue(
                new Callback<ApiResponse<List<Attendance>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Attendance>>> call,
                                   @NonNull Response<ApiResponse<List<Attendance>>> response) {
                if (!isAdded()) return;
                List<Student> roster = buildStudentsFromSelectedCourse();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Attendance> records = response.body().getData();
                    // Add any students that already have a record for the day
                    // but are no longer in the course roster.
                    if (records != null) {
                        for (Attendance a : records) {
                            String sid = a.getStudentId();
                            if (sid == null) continue;
                            boolean inRoster = false;
                            for (Student s : roster) {
                                if (sid.equals(s.getId())) { inRoster = true; break; }
                            }
                            if (!inRoster) {
                                Student known = studentDirectory.get(sid);
                                if (known != null) roster.add(known);
                            }
                        }
                    }
                }
                adapter.setStudents(roster);
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Attendance>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                adapter.setStudents(buildStudentsFromSelectedCourse());
                UIUtils.showToast(requireContext(), "No se pudo cargar la asistencia previa");
            }
        });
    }

    private List<Student> buildStudentsFromSelectedCourse() {
        List<Student> students = new ArrayList<>();
        Course selected = null;
        for (Course c : courses) {
            if (c.getId() != null && c.getId().equals(selectedCourseId)) {
                selected = c;
                break;
            }
        }

        List<String> ids = selected != null ? selected.getStudentIds() : null;
        if (ids == null || ids.isEmpty()) {
            return students;
        }

        for (String id : ids) {
            Student known = studentDirectory.get(id);
            if (known != null) {
                students.add(known);
            } else {
                Student placeholder = new Student();
                placeholder.setId(id);
                placeholder.setFirstName("Estudiante");
                placeholder.setLastName(id.length() > 6 ? id.substring(id.length() - 6) : id);
                students.add(placeholder);
            }
        }
        return students;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            btnDate.setText(selectedDate);
            loadAttendance();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveAttendance() {
        if (selectedCourseId == null) {
            UIUtils.showToast(requireContext(), "Seleccione un curso");
            return;
        }
        Map<String, String> attendanceMap = adapter.getAttendanceMap();
        if (attendanceMap.isEmpty()) {
            UIUtils.showToast(requireContext(), "No hay estudiantes para registrar");
            return;
        }
        List<Map<String, String>> records = new ArrayList<>();
        for (Map.Entry<String, String> entry : attendanceMap.entrySet()) {
            Map<String, String> record = new HashMap<>();
            record.put("studentId", entry.getKey());
            record.put("status", entry.getValue());
            records.add(record);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("courseId", selectedCourseId);
        body.put("date", selectedDate);
        body.put("records", records);

        attendanceDao.saveBulkAttendance(body).enqueue(new Callback<ApiResponse<List<Attendance>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Attendance>>> call,
                                   @NonNull Response<ApiResponse<List<Attendance>>> response) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Asistencia guardada");
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Attendance>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error al guardar");
            }
        });
    }
}
