package com.iepca.app.view.fragment.student;

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

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Grade;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentGradesFragment extends Fragment {

    private Spinner spinnerCourse;
    private RecyclerView rvGrades;
    private CourseDao courseDao;
    private GradeDao gradeDao;
    private GradeSummaryAdapter adapter;
    private List<Course> courses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        gradeDao = RetrofitClient.createService(requireContext(), GradeDao.class);

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        rvGrades = view.findViewById(R.id.rvGrades);

        adapter = new GradeSummaryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(adapter);

        loadCourses();
    }

    private void loadCourses() {
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
                            loadGrades(courses.get(pos).getId());
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

    private void loadGrades(String courseId) {
        gradeDao.getGradesByCourse(courseId, 1).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }
}
