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

    private CourseDao courseDao;
    private AttendanceDao attendanceDao;
    private AttendanceAdapter adapter;
    private List<Course> courses = new ArrayList<>();
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

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        btnDate = view.findViewById(R.id.btnDate);
        btnSave = view.findViewById(R.id.btnSave);
        rvStudents = view.findViewById(R.id.rvStudents);

        adapter = new AttendanceAdapter();
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(adapter);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        btnDate.setText(selectedDate);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveAttendance());

        loadMyCourses();
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
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Attendance> list = response.body().getData();
                    List<Student> students = new ArrayList<>();
                    if (list != null) {
                        for (Attendance a : list) {
                            if (a.getStudent() != null) students.add(a.getStudent());
                        }
                    }
                    if (students.isEmpty()) {
                        students = buildStudentsFromSelectedCourse();
                    }
                    adapter.setStudents(students);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Attendance>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando asistencia");
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
        String[][] demoNames = {
                {"Vargas", "Alejandra"},
                {"Mendoza", "Carlos"},
                {"Ramirez", "Sofia"},
                {"Torres", "Javier"},
                {"Fernandez", "Laura"}
        };

        int count = ids != null && !ids.isEmpty() ? ids.size() : demoNames.length;
        for (int i = 0; i < count; i++) {
            Student student = new Student();
            student.setId(ids != null && i < ids.size() ? ids.get(i) : "demo-student-" + i);
            student.setLastName(demoNames[i % demoNames.length][0]);
            student.setFirstName(demoNames[i % demoNames.length][1]);
            students.add(student);
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
