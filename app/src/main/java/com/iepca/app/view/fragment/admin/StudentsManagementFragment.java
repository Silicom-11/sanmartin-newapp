package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.iepca.app.controller.StudentManagementController;
import com.iepca.app.dao.callback.ApiCallback;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.StudentAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student administration screen.
 *
 * MVC role: View. It renders data and delegates orchestration to
 * StudentManagementController.
 */
public class StudentsManagementFragment extends Fragment implements StudentAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvCount;
    private View emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private StudentManagementController studentController;
    private StudentAdapter adapter;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_students_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        studentController = new StudentManagementController(requireContext());
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadStudents();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        etSearch = view.findViewById(R.id.etSearch);
        tvCount = view.findViewById(R.id.tvCount);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new StudentAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadStudents);
        fabAdd.setOnClickListener(view -> showStudentDialog(null));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                searchQuery = editable.toString().trim();
                loadStudents();
            }
        });
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        studentController.listStudents(searchQuery, 1, 50, new ApiCallback<List<Student>>() {
            @Override
            public void onSuccess(List<Student> students) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                adapter.setItems(students);
                tvCount.setText(students.size() + " registros");
                emptyView.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(students.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), message);
            }
        });
    }

    @Override
    public void onItemClick(Student student) {
        showStudentDialog(student);
    }

    private void showStudentDialog(Student student) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_student_form, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDni);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        Spinner spinnerGradeLevel = dialogView.findViewById(R.id.spinnerGradeLevel);
        Spinner spinnerSection = dialogView.findViewById(R.id.spinnerSection);

        String[] gradeLevels = {"1", "2", "3", "4", "5", "6"};
        String[] sections = {"A", "B", "C", "D"};
        spinnerGradeLevel.setAdapter(new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, gradeLevels));
        spinnerSection.setAdapter(new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, sections));

        if (student != null) {
            etFirstName.setText(student.getFirstName());
            etLastName.setText(student.getLastName());
            etDni.setText(student.getDni());
            selectValue(spinnerGradeLevel, gradeLevels, student.getGradeLevel());
            selectValue(spinnerSection, sections, student.getSection());
        }

        String title = student == null ? "Nuevo Estudiante" : "Editar Estudiante";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", valueOf(etFirstName));
                    data.put("lastName", valueOf(etLastName));
                    data.put("dni", valueOf(etDni));
                    data.put("email", valueOf(etEmail));
                    data.put("gradeLevel", spinnerGradeLevel.getSelectedItem().toString());
                    data.put("section", spinnerSection.getSelectedItem().toString());

                    if (student == null) {
                        createStudent(data);
                    } else {
                        updateStudent(student.getId(), data);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createStudent(Map<String, Object> data) {
        studentController.createStudent(data, new ApiCallback<Student>() {
            @Override
            public void onSuccess(Student student) {
                if (!isAdded()) return;
                UIUtils.showToast(requireContext(), "Estudiante creado");
                loadStudents();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) UIUtils.showToast(requireContext(), message);
            }
        });
    }

    private void updateStudent(String id, Map<String, Object> data) {
        studentController.updateStudent(id, data, new ApiCallback<Student>() {
            @Override
            public void onSuccess(Student student) {
                if (!isAdded()) return;
                UIUtils.showToast(requireContext(), "Estudiante actualizado");
                loadStudents();
            }

            @Override
            public void onError(String message) {
                if (isAdded()) UIUtils.showToast(requireContext(), message);
            }
        });
    }

    private void selectValue(Spinner spinner, String[] values, String selected) {
        for (int index = 0; index < values.length; index++) {
            if (values[index].equals(selected)) {
                spinner.setSelection(index);
                return;
            }
        }
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
