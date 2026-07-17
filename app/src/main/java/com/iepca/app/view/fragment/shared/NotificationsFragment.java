package com.iepca.app.view.fragment.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.NotificationDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Notification;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.NotificationAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvNotifications;
    private NotificationDao notificationDao;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notificationDao = RetrofitClient.createService(requireContext(), NotificationDao.class);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvNotifications = view.findViewById(R.id.rvNotifications);

        adapter = new NotificationAdapter(this::markAsRead);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadNotifications);
        loadNotifications();
    }

    private void markAsRead(Notification notification) {
        if (notification.isRead() || notification.getId() == null) return;
        notificationDao.markAsRead(notification.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                if (isAdded()) loadNotifications();
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {}
        });
    }

    private void loadNotifications() {
        notificationDao.getNotifications(1, 50).enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Notification>>> call,
                                   @NonNull Response<ApiResponse<List<Notification>>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Notification>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                UIUtils.showToast(requireContext(), "Error cargando notificaciones");
            }
        });
    }
}
