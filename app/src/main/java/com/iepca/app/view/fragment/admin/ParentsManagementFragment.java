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
import com.iepca.app.dao.ParentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Parent;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.ParentAdapter;

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
                UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    @Override
    public void onItemClick(Parent parent) {
        showParentDialog(parent);
    }

    private void showParentDialog(Parent parent) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_student_form, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etDni = dialogView.findViewById(R.id.etDni);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);

        if (parent != null) {
            etFirstName.setText(parent.getFirstName());
            etLastName.setText(parent.getLastName());
            etDni.setText(parent.getDni());
            etEmail.setText(parent.getEmail());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(parent == null ? "Nuevo Padre" : "Editar Padre")
                .setView(dialogView)
                .setPositiveButton("Guardar", (d, w) -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", etFirstName.getText().toString().trim());
                    data.put("lastName", etLastName.getText().toString().trim());
                    data.put("dni", etDni.getText().toString().trim());
                    data.put("email", etEmail.getText().toString().trim());
                    if (parent == null) {
                        parentDao.createParent(data).enqueue(new Callback<ApiResponse<Parent>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Parent>> c, @NonNull Response<ApiResponse<Parent>> r) { if (isAdded()) { UIUtils.showToast(requireContext(), "Padre creado"); loadParents(); } }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Parent>> c, @NonNull Throwable t) {}
                        });
                    } else {
                        parentDao.updateParent(parent.getId(), data).enqueue(new Callback<ApiResponse<Parent>>() {
                            @Override public void onResponse(@NonNull Call<ApiResponse<Parent>> c, @NonNull Response<ApiResponse<Parent>> r) { if (isAdded()) { UIUtils.showToast(requireContext(), "Padre actualizado"); loadParents(); } }
                            @Override public void onFailure(@NonNull Call<ApiResponse<Parent>> c, @NonNull Throwable t) {}
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
