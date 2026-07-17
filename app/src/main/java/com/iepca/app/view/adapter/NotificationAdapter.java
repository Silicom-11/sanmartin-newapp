package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for notifications.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    private List<Notification> items = new ArrayList<>();
    private OnItemClickListener listener;

    public NotificationAdapter() {}

    public NotificationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Notification> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvBody;
        private final TextView tvTime;
        private final View viewDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewDot = itemView.findViewById(R.id.viewDot);
        }

        void bind(Notification n) {
            tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Notificacion");
            tvBody.setText(n.getMessage() != null ? n.getMessage() : "");
            tvTime.setText(formatTime(n.getCreatedAt()));
            viewDot.setVisibility(n.isRead() ? View.INVISIBLE : View.VISIBLE);
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(n);
            });
        }

        private String formatTime(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return "";
            }
            if (raw.length() >= 16 && raw.contains("T")) {
                return raw.substring(11, 16);
            }
            return raw.length() > 10 ? raw.substring(0, 10) : raw;
        }
    }
}
