package com.iepca.app.view.fragment.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.dao.NotificationDao;
import com.iepca.app.dao.ParentDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Notification;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.ChildCardAdapter;
import com.iepca.app.view.adapter.NotificationAdapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentDashboardFragment extends Fragment {

    private TextView tvWelcome;
    private RecyclerView rvChildren, rvNotifications;
    private MaterialButton btnGrades, btnLocation, btnJustify;
    private StudentDao studentDao;
    private ParentDao parentDao;
    private NotificationDao notificationDao;
    private ChildCardAdapter childAdapter;
    private NotificationAdapter notificationAdapter;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = SessionManager.getInstance(requireContext());
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);
        parentDao = RetrofitClient.createService(requireContext(), ParentDao.class);
        notificationDao = RetrofitClient.createService(requireContext(), NotificationDao.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        rvChildren = view.findViewById(R.id.rvChildren);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        btnGrades = view.findViewById(R.id.btnGrades);
        btnLocation = view.findViewById(R.id.btnLocation);
        btnJustify = view.findViewById(R.id.btnJustify);

        tvWelcome.setText("Bienvenido, " + session.getUserName());

        childAdapter = new ChildCardAdapter(child ->
                UIUtils.showToast(requireContext(), child.getFullName()));
        rvChildren.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChildren.setAdapter(childAdapter);

        notificationAdapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(notificationAdapter);

        btnGrades.setOnClickListener(v -> navigateToNav(R.id.nav_grades, new ChildrenGradesFragment()));
        btnLocation.setOnClickListener(v -> navigateToNav(R.id.nav_gps, new ChildrenLocationFragment()));
        btnJustify.setOnClickListener(v -> navigateToNav(R.id.nav_justifications, new JustificationFormFragment()));

        loadChildren();
        loadNotifications();
    }

    private void loadChildren() {
        studentDao.getMyChildren().enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Student> children = response.body().getData();
                    childAdapter.setItems(children);
                    loadAttendanceRates(children);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando hijos");
            }
        });
    }

    private void loadAttendanceRates(List<Student> children) {
        if (children == null) return;
        for (Student child : children) {
            parentDao.getChildAttendance(child.getId()).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call,
                                       @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        childAdapter.setAttendanceRate(child.getId(), calculateAttendanceRate(response.body().getData()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                    // Dashboard remains usable even if this optional KPI fails.
                }
            });
        }
    }

    private double calculateAttendanceRate(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) return 0;
        int ok = 0;
        for (Map<String, Object> item : records) {
            Object raw = item.get("status");
            String status = raw == null ? "" : String.valueOf(raw).toLowerCase(Locale.ROOT);
            if ("present".equals(status) || "justified".equals(status)) {
                ok++;
            }
        }
        return ok * 100.0 / records.size();
    }

    private void loadNotifications() {
        notificationDao.getNotifications(1, 5).enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Notification>>> call,
                                   @NonNull Response<ApiResponse<List<Notification>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    notificationAdapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Notification>>> call, @NonNull Throwable t) {}
        });
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() instanceof com.iepca.app.view.activity.MainActivity) {
            ((com.iepca.app.view.activity.MainActivity) getActivity()).loadFragment(fragment);
        }
    }

    private void navigateToNav(int navItemId, Fragment fallback) {
        if (getActivity() instanceof com.iepca.app.view.activity.MainActivity) {
            ((com.iepca.app.view.activity.MainActivity) getActivity()).selectBottomNavItem(navItemId);
        } else {
            navigateTo(fallback);
        }
    }
}
