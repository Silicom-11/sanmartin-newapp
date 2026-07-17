package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.CourseDao;
import com.iepca.app.dao.GradeDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Grade;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeSummaryAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin view of every grade registered in the institution, with student
 * and course names resolved to real data.
 */
public class GradesManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View emptyView;
    private TextView tvCount;

    private GradeDao gradeDao;
    private CourseDao courseDao;
    private StudentDao studentDao;
    private GradeSummaryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grades_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gradeDao = RetrofitClient.createService(requireContext(), GradeDao.class);
        courseDao = RetrofitClient.createService(requireContext(), CourseDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        tvCount = view.findViewById(R.id.tvCount);

        adapter = new GradeSummaryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::refreshAll);
        refreshAll();
    }

    private void refreshAll() {
        loadCourseNames();
        loadStudentNames();
        loadGrades();
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

    private void loadStudentNames() {
        studentDao.getStudents(null, null, null, 1, 200).enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Student> students = response.body().getData();
                    Map<String, String> names = new HashMap<>();
                    if (students != null) {
                        for (Student s : students) {
                            if (s.getId() != null) names.put(s.getId(), s.getFullName());
                        }
                    }
                    adapter.setStudentNames(names);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {}
        });
    }

    private void loadGrades() {
        progressBar.setVisibility(View.VISIBLE);
        gradeDao.getAllGrades().enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Grade> grades = response.body().getData();
                    adapter.setItems(grades);
                    int count = grades != null ? grades.size() : 0;
                    tvCount.setText(count + " registros");
                    boolean empty = count == 0;
                    emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), "Error cargando calificaciones");
            }
        });
    }
}
