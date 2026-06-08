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
import com.iepca.app.dao.TeacherDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Teacher;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.TeacherAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeachersManagementFragment extends Fragment implements TeacherAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvCount;
    private View emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private TeacherDao teacherDao;
    private TeacherAdapter adapter;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teachers_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teacherDao = RetrofitClient.createService(requireContext(), TeacherDao.class);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadTeachers();
    }

    private void initViews(View v) {
        recyclerView = v.findViewById(R.id.recyclerView);
        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        etSearch = v.findViewById(R.id.etSearch);
        tvCount = v.findViewById(R.id.tvCount);
        emptyView = v.findViewById(R.id.emptyView);
        progressBar = v.findViewById(R.id.progressBar);
        fabAdd = v.findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new TeacherAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadTeachers);
        fabAdd.setOnClickListener(v -> showTeacherDialog(null));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                loadTeachers();
            }
        });
    }

    private void loadTeachers() {
        progressBar.setVisibility(View.VISIBLE);
        teacherDao.getTeachers(searchQuery, null, 1, 50).enqueue(
                new Callback<ApiResponse<List<Teacher>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Teacher>>> call,
                                   @NonNull Response<ApiResponse<List<Teacher>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Teacher> list = response.body().getData();
                    adapter.setItems(list);
                    tvCount.setText(list.size() + " registros");
                    emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Teacher>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    @Override
    public void onItemClick(Teacher teacher) {
        showTeacherDialog(teacher);
    }

    private void showTeacherDialog(Teacher teacher) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_student_form, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDni);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        if (teacher != null) {
            etFirstName.setText(teacher.getFirstName());
            etLastName.setText(teacher.getLastName());
            etDni.setText(teacher.getDni());
            etEmail.setText(teacher.getEmail());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(teacher == null ? "Nuevo Docente" : "Editar Docente")
                .setView(dialogView)
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", etFirstName.getText().toString().trim());
                    data.put("lastName", etLastName.getText().toString().trim());
                    data.put("dni", etDni.getText().toString().trim());
                    data.put("email", etEmail.getText().toString().trim());
                    if (teacher == null) {
                        teacherDao.createTeacher(data).enqueue(new Callback<ApiResponse<Teacher>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Teacher>> c, @NonNull Response<ApiResponse<Teacher>> r) { if (isAdded()) { UIUtils.showToast(requireContext(), "Docente creado"); loadTeachers(); } }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Teacher>> c, @NonNull Throwable t) { if (isAdded()) UIUtils.showToast(requireContext(), "Error"); }
                        });
                    } else {
                        teacherDao.updateTeacher(teacher.getId(), data).enqueue(new Callback<ApiResponse<Teacher>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Teacher>> c, @NonNull Response<ApiResponse<Teacher>> r) { if (isAdded()) { UIUtils.showToast(requireContext(), "Docente actualizado"); loadTeachers(); } }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Teacher>> c, @NonNull Throwable t) { if (isAdded()) UIUtils.showToast(requireContext(), "Error"); }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
