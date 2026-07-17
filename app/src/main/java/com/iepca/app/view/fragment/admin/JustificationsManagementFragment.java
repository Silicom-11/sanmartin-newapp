package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
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

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.JustificationDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Justification;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.JustificationAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JustificationsManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View emptyView;
    private TextView tvCount;

    private JustificationDao justificationDao;
    private StudentDao studentDao;
    private JustificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_justifications_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        justificationDao = RetrofitClient.createService(requireContext(), JustificationDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
        tvCount = view.findViewById(R.id.tvCount);

        adapter = new JustificationAdapter(new JustificationAdapter.OnActionListener() {
            @Override
            public void onApprove(Justification j) { reviewJustification(j.getId(), "approved"); }
            @Override
            public void onReject(Justification j) { reviewJustification(j.getId(), "rejected"); }
            @Override
            public void onItemClick(Justification j) {}
        }, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadJustifications);
        loadStudentNames();
        loadJustifications();
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

    private void loadJustifications() {
        progressBar.setVisibility(View.VISIBLE);
        justificationDao.getJustifications(null, null, 1, 50).enqueue(
                new Callback<ApiResponse<List<Justification>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Justification>>> call,
                                   @NonNull Response<ApiResponse<List<Justification>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Justification> list = response.body().getData();
                    adapter.setItems(list);
                    tvCount.setText(list.size() + " registros");
                    emptyView.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Justification>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                UIUtils.showToast(requireContext(), "Error de conexión");
            }
        });
    }

    private void reviewJustification(String id, String status) {
        Map<String, String> data = new HashMap<>();
        data.put("status", status);
        justificationDao.reviewJustification(id, data).enqueue(new Callback<ApiResponse<Justification>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Justification>> call,
                                   @NonNull Response<ApiResponse<Justification>> response) {
                if (isAdded()) {
                    String label = "approved".equals(status) ? "aprobada" : "rechazada";
                    UIUtils.showToast(requireContext(), "Justificación " + label);
                    loadJustifications();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Justification>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error al procesar");
            }
        });
    }
}
