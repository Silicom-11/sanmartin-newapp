package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.ParentDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Parent;
import com.iepca.app.model.ParentChild;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.ParentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentsManagementFragment extends Fragment implements ParentAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private TextView tvCount;
    private View emptyView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;

    private ParentDao parentDao;
    private StudentDao studentDao;
    private ParentAdapter adapter;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parents_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentDao = RetrofitClient.createService(requireContext(), ParentDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);
        initViews(view);
        adapter = new ParentAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        swipeRefresh.setOnRefreshListener(this::loadParents);
        fabAdd.setOnClickListener(v -> showParentDialog(null));
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { searchQuery = s.toString().trim(); loadParents(); }
        });
        loadParents();
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

    private void loadParents() {
        progressBar.setVisibility(View.VISIBLE);
        parentDao.getParents(1, 50, searchQuery).enqueue(new Callback<ApiResponse<List<Parent>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Parent>>> call,
                                   @NonNull Response<ApiResponse<List<Parent>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Parent> list = response.body().getData();
                    adapter.setItems(list);
                    tvCount.setText(list.size() + " registros");
                    emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Parent>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), "Error de conexion");
            }
        });
    }

    @Override
    public void onItemClick(Parent parent) {
        showParentOptionsDialog(parent);
    }

    private void showParentOptionsDialog(Parent parent) {
        String[] options = {"Editar datos", "Vincular hijo", "Desvincular hijo", "Desactivar"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(parent.getFullName())
                .setItems(options, (d, which) -> {
                    switch (which) {
                        case 0: showParentDialog(parent); break;
                        case 1: showLinkChildDialog(parent); break;
                        case 2: showUnlinkChildDialog(parent); break;
                        case 3: deleteParent(parent); break;
                    }
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void showParentDialog(Parent parent) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_parent_form, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDni);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etOccupation = dialogView.findViewById(R.id.etOccupation);
        TextInputEditText etWorkplace = dialogView.findViewById(R.id.etWorkplace);

        if (parent != null) {
            etFirstName.setText(parent.getFirstName());
            etLastName.setText(parent.getLastName());
            etDni.setText(parent.getDni());
            etEmail.setText(parent.getEmail());
            etPhone.setText(parent.getPhone());
            etOccupation.setText(parent.getOccupation());
            etWorkplace.setText(parent.getWorkplace());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(parent == null ? "Nuevo Padre/Apoderado" : "Editar Padre/Apoderado")
                .setView(dialogView)
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", etFirstName.getText().toString().trim());
                    data.put("lastName", etLastName.getText().toString().trim());
                    data.put("dni", etDni.getText().toString().trim());
                    data.put("email", etEmail.getText().toString().trim());
                    data.put("phone", etPhone.getText().toString().trim());
                    data.put("occupation", etOccupation.getText().toString().trim());
                    data.put("workplace", etWorkplace.getText().toString().trim());

                    if (parent == null) {
                        parentDao.createParent(data).enqueue(new Callback<ApiResponse<Parent>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Parent>> c, @NonNull Response<ApiResponse<Parent>> r) {
                                if (!isAdded()) return;
                                if (r.isSuccessful()) {
                                    UIUtils.showToast(requireContext(), "Padre creado (contrasena = DNI)");
                                    loadParents();
                                }
                            }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Parent>> c, @NonNull Throwable t) {
                                if (isAdded()) UIUtils.showToast(requireContext(), "Error al crear padre");
                            }
                        });
                    } else {
                        parentDao.updateParent(parent.getId(), data).enqueue(new Callback<ApiResponse<Parent>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Parent>> c, @NonNull Response<ApiResponse<Parent>> r) {
                                if (!isAdded()) return;
                                UIUtils.showToast(requireContext(), "Padre actualizado");
                                loadParents();
                            }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Parent>> c, @NonNull Throwable t) {
                                if (isAdded()) UIUtils.showToast(requireContext(), "Error al actualizar");
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showLinkChildDialog(Parent parent) {
        studentDao.getStudentsManagement(1, 100, null, null).enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    UIUtils.showToast(requireContext(), "Error cargando estudiantes");
                    return;
                }
                List<Student> allStudents = response.body().getData();
                if (allStudents == null || allStudents.isEmpty()) {
                    UIUtils.showToast(requireContext(), "No hay estudiantes registrados");
                    return;
                }

                List<String> linkedIds = new ArrayList<>();
                if (parent.getChildren() != null) {
                    for (ParentChild pc : parent.getChildren()) {
                        linkedIds.add(pc.getStudentId());
                    }
                }
                List<Student> available = new ArrayList<>();
                for (Student s : allStudents) {
                    if (!linkedIds.contains(s.getId())) {
                        available.add(s);
                    }
                }
                if (available.isEmpty()) {
                    UIUtils.showToast(requireContext(), "Todos los estudiantes ya estan vinculados");
                    return;
                }

                String[] names = new String[available.size()];
                for (int i = 0; i < available.size(); i++) {
                    names[i] = available.get(i).getFullName() + " (" + available.get(i).getDni() + ")";
                }

                View dialogView = LayoutInflater.from(requireContext()).inflate(
                        android.R.layout.simple_list_item_1, null, false);

                final int[] selectedIndex = {0};
                String[] relationships = {"padre", "madre", "tutor", "abuelo/a", "tio/a", "otro"};

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Vincular hijo a " + parent.getFullName())
                        .setSingleChoiceItems(names, 0, (d, which) -> selectedIndex[0] = which)
                        .setPositiveButton("Vincular", (d, w) -> {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Relacion con el estudiante")
                                    .setItems(relationships, (d2, relIdx) -> {
                                        Student selected = available.get(selectedIndex[0]);
                                        Map<String, String> body = new HashMap<>();
                                        body.put("student", selected.getId());
                                        body.put("relationship", relationships[relIdx]);
                                        linkChild(parent.getId(), body);
                                    })
                                    .show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexion");
            }
        });
    }

    private void linkChild(String parentId, Map<String, String> body) {
        parentDao.linkChild(parentId, body).enqueue(new Callback<ApiResponse<Parent>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Parent>> call,
                                   @NonNull Response<ApiResponse<Parent>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    UIUtils.showToast(requireContext(), "Hijo vinculado exitosamente");
                    loadParents();
                } else {
                    UIUtils.showToast(requireContext(), "Error al vincular hijo");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Parent>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexion");
            }
        });
    }

    private void showUnlinkChildDialog(Parent parent) {
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            UIUtils.showToast(requireContext(), "Este padre no tiene hijos vinculados");
            return;
        }

        String[] childNames = new String[parent.getChildren().size()];
        for (int i = 0; i < parent.getChildren().size(); i++) {
            ParentChild pc = parent.getChildren().get(i);
            childNames[i] = pc.getStudentId() + " (" + pc.getRelationship() + ")";
        }

        studentDao.getStudentsManagement(1, 100, null, null).enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                List<Student> students = (response.isSuccessful() && response.body() != null && response.body().isSuccess())
                        ? response.body().getData() : new ArrayList<>();

                String[] displayNames = new String[parent.getChildren().size()];
                for (int i = 0; i < parent.getChildren().size(); i++) {
                    ParentChild pc = parent.getChildren().get(i);
                    String studentName = pc.getStudentId();
                    for (Student s : students) {
                        if (s.getId().equals(pc.getStudentId())) {
                            studentName = s.getFullName();
                            break;
                        }
                    }
                    displayNames[i] = studentName + " (" + pc.getRelationship() + ")";
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Desvincular hijo de " + parent.getFullName())
                        .setItems(displayNames, (d, which) -> {
                            String studentId = parent.getChildren().get(which).getStudentId();
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Confirmar")
                                    .setMessage("Desvincular a " + displayNames[which] + "?")
                                    .setPositiveButton("Desvincular", (d2, w2) -> unlinkChild(parent.getId(), studentId))
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexion");
            }
        });
    }

    private void unlinkChild(String parentId, String studentId) {
        parentDao.unlinkChild(parentId, studentId).enqueue(new Callback<ApiResponse<Parent>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Parent>> call,
                                   @NonNull Response<ApiResponse<Parent>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    UIUtils.showToast(requireContext(), "Hijo desvinculado");
                    loadParents();
                } else {
                    UIUtils.showToast(requireContext(), "Error al desvincular");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Parent>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error de conexion");
            }
        });
    }

    private void deleteParent(Parent parent) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Desactivar padre")
                .setMessage("Desactivar a " + parent.getFullName() + "?")
                .setPositiveButton("Desactivar", (d, w) -> {
                    parentDao.deleteParent(parent.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                               @NonNull Response<ApiResponse<Void>> response) {
                            if (isAdded()) {
                                UIUtils.showToast(requireContext(), "Padre desactivado");
                                loadParents();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {}
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
