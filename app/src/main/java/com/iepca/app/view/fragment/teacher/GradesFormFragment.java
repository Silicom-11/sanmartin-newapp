package com.iepca.app.view.fragment.teacher;

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
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Evaluation;
import com.iepca.app.model.Grade;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradesFormFragment extends Fragment {

    private Spinner spinnerCourse, spinnerEvaluation;
    private RecyclerView rvGrades;
    private MaterialButton btnSave;

    private CourseDao courseDao;
    private GradeDao gradeDao;
    private GradeSummaryAdapter gradeAdapter;
    private List<Course> courses = new ArrayList<>();
    private List<Evaluation> evaluations = new ArrayList<>();
    private String selectedCourseId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grades_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        gradeDao = RetrofitClient.createService(requireContext(), GradeDao.class);

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        spinnerEvaluation = view.findViewById(R.id.spinnerEvaluation);
        rvGrades = view.findViewById(R.id.rvGrades);
        btnSave = view.findViewById(R.id.btnSave);

        gradeAdapter = new GradeSummaryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(gradeAdapter);

        btnSave.setOnClickListener(v -> UIUtils.showToast(requireContext(), "Calificaciones guardadas"));

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
                            loadEvaluations();
                            loadGrades();
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

    private void loadEvaluations() {
        if (selectedCourseId == null) return;
        gradeDao.getEvaluations(selectedCourseId, 1).enqueue(new Callback<ApiResponse<List<Evaluation>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Evaluation>>> call,
                                   @NonNull Response<ApiResponse<List<Evaluation>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    evaluations = response.body().getData();
                    List<String> names = new ArrayList<>();
                    for (Evaluation e : evaluations) names.add(e.getName());
                    if (names.isEmpty()) names.add("Sin evaluaciones");
                    spinnerEvaluation.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Evaluation>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadGrades() {
        if (selectedCourseId == null) return;
        gradeDao.getGradesByCourse(selectedCourseId, 1).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Grade> grades = response.body().getData();
                    if (grades == null || grades.isEmpty()) {
                        grades = buildDemoGradeRows();
                    }
                    gradeAdapter.setItems(grades);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }

    private List<Grade> buildDemoGradeRows() {
        List<Grade> rows = new ArrayList<>();
        String[] names = {"Alejandra Vargas", "Carlos Mendoza", "Sofia Ramirez", "Javier Torres", "Laura Fernandez"};
        int[] scores = {18, 15, 17, 0, 16};
        for (int i = 0; i < names.length; i++) {
            Grade grade = new Grade();
            grade.setCourseId(names[i]);
            grade.setBimester(1);
            grade.setScore(scores[i]);
            rows.add(grade);
        }
        return rows;
    }
}
