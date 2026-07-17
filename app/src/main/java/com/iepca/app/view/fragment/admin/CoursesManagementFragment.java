package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.TeacherDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Teacher;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.CourseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoursesManagementFragment extends Fragment implements CourseAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvCount;
    private View emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private CourseDao courseDao;
    private TeacherDao teacherDao;
    private CourseAdapter adapter;
    private List<Teacher> teachers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        teacherDao = RetrofitClient.createService(requireContext(), TeacherDao.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        etSearch = view.findViewById(R.id.etSearch);
        tvCount = view.findViewById(R.id.tvCount);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        fabAdd = view.findViewById(R.id.fabAdd);

        adapter = new CourseAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadCourses);
        fabAdd.setOnClickListener(v -> showCourseDialog(null));
        loadTeachers();
        loadCourses();
    }

    private void loadTeachers() {
        teacherDao.getTeachers(null, null, 1, 100).enqueue(new Callback<ApiResponse<List<Teacher>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Teacher>>> call,
                                   @NonNull Response<ApiResponse<List<Teacher>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    teachers = response.body().getData();
                    if (teachers == null) teachers = new ArrayList<>();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Teacher>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);
        courseDao.getCourses(null, null, null, 1, 50).enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Course>>> call,
                                   @NonNull Response<ApiResponse<List<Course>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Course> list = response.body().getData();
                    adapter.setItems(list);
                    tvCount.setText(list.size() + " registros");
                    emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Course>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    @Override
    public void onItemClick(Course course) {
        showCourseDialog(course);
    }

    private void showCourseDialog(@Nullable Course course) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_course_form, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etCode = dialogView.findViewById(R.id.etCode);
        Spinner spinnerGradeLevel = dialogView.findViewById(R.id.spinnerGradeLevel);
        Spinner spinnerSection = dialogView.findViewById(R.id.spinnerSection);
        Spinner spinnerTeacher = dialogView.findViewById(R.id.spinnerTeacher);

        String[] gradeLevels = {"1° Secundaria", "2° Secundaria", "3° Secundaria",
                "4° Secundaria", "5° Secundaria"};
        String[] sections = {"A", "B", "C", "D"};
        spinnerGradeLevel.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, gradeLevels));
        spinnerSection.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, sections));

        List<String> teacherNames = new ArrayList<>();
        for (Teacher t : teachers) teacherNames.add(t.getFullName());
        if (teacherNames.isEmpty()) teacherNames.add("Sin docentes registrados");
        spinnerTeacher.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, teacherNames));

        if (course != null) {
            etName.setText(course.getName());
            etCode.setText(course.getCode());
            selectValue(spinnerGradeLevel, gradeLevels, course.getGradeLevel());
            selectValue(spinnerSection, sections, course.getSection());
            if (course.getTeacherId() != null) {
                for (int i = 0; i < teachers.size(); i++) {
                    if (course.getTeacherId().equals(teachers.get(i).getId())) {
                        spinnerTeacher.setSelection(i);
                        break;
                    }
                }
            }
        }

        String title = course == null ? "Nuevo Curso" : "Editar Curso";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        UIUtils.showToast(requireContext(), "El nombre del curso es obligatorio");
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("code", etCode.getText() != null ? etCode.getText().toString().trim() : "");
                    data.put("gradeLevel", spinnerGradeLevel.getSelectedItem().toString());
                    data.put("section", spinnerSection.getSelectedItem().toString());
                    int teacherPos = spinnerTeacher.getSelectedItemPosition();
                    if (teacherPos >= 0 && teacherPos < teachers.size()) {
                        data.put("teacher", teachers.get(teacherPos).getId());
                    }
                    if (course == null) {
                        createCourse(data);
                    } else {
                        updateCourse(course.getId(), data);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createCourse(Map<String, Object> data) {
        courseDao.createCourse(data).enqueue(new Callback<ApiResponse<Course>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Course>> call,
                                   @NonNull Response<ApiResponse<Course>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UIUtils.showToast(requireContext(), "Curso creado");
                    loadCourses();
                } else {
                    UIUtils.showToast(requireContext(), "No se pudo crear el curso");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Course>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    private void updateCourse(String id, Map<String, Object> data) {
        courseDao.updateCourse(id, data).enqueue(new Callback<ApiResponse<Course>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Course>> call,
                                   @NonNull Response<ApiResponse<Course>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UIUtils.showToast(requireContext(), "Curso actualizado");
                    loadCourses();
                } else {
                    UIUtils.showToast(requireContext(), "No se pudo actualizar el curso");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Course>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    private void selectValue(Spinner spinner, String[] values, String selected) {
        if (selected == null) return;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(selected)) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
