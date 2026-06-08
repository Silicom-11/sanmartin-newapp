package com.iepca.app.view.fragment.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.EventDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Event;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.EventAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvEvents;
    private EventDao eventDao;
    private EventAdapter eventAdapter;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventDao = RetrofitClient.createService(requireContext(), EventDao.class);

        calendarView = view.findViewById(R.id.calendarView);
        rvEvents = view.findViewById(R.id.rvEvents);

        eventAdapter = new EventAdapter();
        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEvents.setAdapter(eventAdapter);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        calendarView.setOnDateChangeListener((v, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            loadEvents();
        });

        loadEvents();
    }

    private void loadEvents() {
        eventDao.getEvents(selectedDate, selectedDate, null).enqueue(
                new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Event>>> call,
                                   @NonNull Response<ApiResponse<List<Event>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    eventAdapter.setItems(response.body().getData());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando eventos");
            }
        });
    }
}
