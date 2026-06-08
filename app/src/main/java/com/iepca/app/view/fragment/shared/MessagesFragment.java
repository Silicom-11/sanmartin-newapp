package com.iepca.app.view.fragment.shared;

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

import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.MessageDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Conversation;
import com.iepca.app.util.UIUtils;
import com.iepca.app.view.adapter.ConversationAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesFragment extends Fragment {

    private RecyclerView rvConversations;
    private TextView tvEmpty;
    private MessageDao messageDao;
    private ConversationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messageDao = RetrofitClient.createService(requireContext(), MessageDao.class);

        rvConversations = view.findViewById(R.id.rvConversations);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new ConversationAdapter(conversation ->
                UIUtils.showToast(requireContext(), "Chat: " + conversation.getName()));
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(adapter);

        loadConversations();
    }

    private void loadConversations() {
        messageDao.getConversations().enqueue(new Callback<ApiResponse<List<Conversation>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Conversation>>> call,
                                   @NonNull Response<ApiResponse<List<Conversation>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Conversation> list = response.body().getData();
                    adapter.setItems(list);
                    tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Conversation>>> call, @NonNull Throwable t) {
                if (isAdded()) UIUtils.showToast(requireContext(), "Error cargando mensajes");
            }
        });
    }
}
