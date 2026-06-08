package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the conversations list.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private List<Conversation> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public ConversationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Conversation> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
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
        private final TextView tvName;
        private final TextView tvLastMessage;
        private final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(Conversation c) {
            String name = c.getName();
            if (name == null || name.trim().isEmpty()) {
                name = "Conversaci\u00f3n directa";
            }

            tvName.setText(name);
            tvLastMessage.setText(c.getLastMessage() != null && c.getLastMessage().getContent() != null
                    ? c.getLastMessage().getContent()
                    : "Sin mensajes recientes");
            tvTime.setText(formatTime(c.getUpdatedAt()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(c);
                }
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
