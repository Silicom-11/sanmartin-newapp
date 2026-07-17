package com.iepca.app.view.fragment.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;

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
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Course;
import com.iepca.app.model.Evaluation;
import com.iepca.app.model.Grade;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.GradeEntryAdapter;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Teacher grade-entry form.
 *
 * The teacher picks a course and an evaluation, types the 0-20 score for
 * each real student of the course and saves everything in one bulk call.
 */
public class GradesFormFragment extends Fragment {

    private Spinner spinnerCourse, spinnerEvaluation;
    private RecyclerView rvGrades;
    private MaterialButton btnSave;
    private TextView tvEmpty;

    private CourseDao courseDao;
    private GradeDao gradeDao;
    private StudentDao studentDao;
    private GradeEntryAdapter gradeEntryAdapter;
    private List<Course> courses = new ArrayList<>();
    private List<Evaluation> evaluations = new ArrayList<>();
    private final Map<String, Student> studentDirectory = new HashMap<>();
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
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        spinnerEvaluation = view.findViewById(R.id.spinnerEvaluation);
        rvGrades = view.findViewById(R.id.rvGrades);
        btnSave = view.findViewById(R.id.btnSave);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        gradeEntryAdapter = new GradeEntryAdapter();
        rvGrades.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrades.setAdapter(gradeEntryAdapter);

        btnSave.setOnClickListener(v -> saveGrades());

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
                    if (names.isEmpty()) {
                        names.add("Sin cursos asignados");
                        btnSave.setEnabled(false);
                    }
                    spinnerCourse.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                    spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                            if (pos < courses.size()) {
                                selectedCourseId = courses.get(pos).getId();
                                loadEvaluations();
                                showCourseRoster();
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

    /** Shows the editable list with the real students enrolled in the course. */
    private void showCourseRoster() {
        List<Student> roster = new ArrayList<>();
        for (Course c : courses) {
            if (c.getId() != null && c.getId().equals(selectedCourseId)) {
                List<String> ids = c.getStudentIds();
                if (ids != null) {
                    for (String id : ids) {
                        Student known = studentDirectory.get(id);
                        if (known != null) {
                            roster.add(known);
                        } else {
                            Student placeholder = new Student();
                            placeholder.setId(id);
                            placeholder.setFirstName("Estudiante");
                            placeholder.setLastName(id.length() > 6 ? id.substring(id.length() - 6) : id);
                            roster.add(placeholder);
                        }
                    }
                }
                break;
            }
        }
        gradeEntryAdapter.setStudents(roster);
        boolean empty = roster.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvGrades.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnSave.setEnabled(!empty);
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
                    if (evaluations == null) evaluations = new ArrayList<>();
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

    private void saveGrades() {
        if (selectedCourseId == null) {
            UIUtils.showToast(requireContext(), "Seleccione un curso");
            return;
        }
        int evalPosition = spinnerEvaluation.getSelectedItemPosition();
        if (evaluations.isEmpty() || evalPosition < 0 || evalPosition >= evaluations.size()) {
            UIUtils.showToast(requireContext(), "El curso no tiene evaluaciones configuradas");
            return;
        }
        Map<String, Double> scores = gradeEntryAdapter.getGradesMap();
        if (scores.isEmpty()) {
            UIUtils.showToast(requireContext(), "Ingresa al menos una nota (0 a 20)");
            return;
        }

        String evaluationId = evaluations.get(evalPosition).getId();
        List<Map<String, Object>> gradeEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            Map<String, Object> gradeEntry = new HashMap<>();
            gradeEntry.put("studentId", entry.getKey());
            gradeEntry.put("evaluationId", evaluationId);
            gradeEntry.put("score", entry.getValue());
            gradeEntries.add(gradeEntry);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("courseId", selectedCourseId);
        body.put("bimester", 1);
        body.put("grades", gradeEntries);

        btnSave.setEnabled(false);
        gradeDao.saveBulk(body).enqueue(new Callback<ApiResponse<List<Grade>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Grade>>> call,
                                   @NonNull Response<ApiResponse<List<Grade>>> response) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UIUtils.showToast(requireContext(),
                            "Calificaciones guardadas (" + scores.size() + " estudiantes)");
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage() : "No se pudieron guardar las notas";
                    UIUtils.showToast(requireContext(), msg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Grade>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnSave.setEnabled(true);
                UIUtils.showToast(requireContext(), "Error de conexión al guardar");
            }
        });
    }
}
