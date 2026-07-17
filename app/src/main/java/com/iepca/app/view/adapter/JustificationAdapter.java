package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.model.Justification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView Adapter for Justifications list.
 */
public class JustificationAdapter extends RecyclerView.Adapter<JustificationAdapter.ViewHolder> {

    private List<Justification> items = new ArrayList<>();
    private Map<String, String> studentNames = new HashMap<>();
    private OnActionListener listener;
    private boolean showActions;

    public interface OnActionListener {
        void onApprove(Justification j);
        void onReject(Justification j);
        void onItemClick(Justification j);
    }

    public JustificationAdapter(OnActionListener listener, boolean showActions) {
        this.listener = listener;
        this.showActions = showActions;
    }

    public void setItems(List<Justification> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** studentId -> display name */
    public void setStudentNames(Map<String, String> names) {
        this.studentNames = names != null ? names : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_justification, parent, false);
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
        private final TextView tvStudentName, tvReason, tvDates, tvStatus;
        private final View layoutActions;
        private final MaterialButton btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(Justification j) {
            String studentName = j.getStudentId() != null ? studentNames.get(j.getStudentId()) : null;
            if (studentName == null) studentName = "Estudiante";
            tvStudentName.setText(studentName);
            tvReason.setText("Motivo: " + (j.getReason() != null ? j.getReason() : "--"));
            String dates = j.getDates() != null && !j.getDates().isEmpty() ? formatDate(j.getDates().get(0)) : "";
            tvDates.setText(dates);
            String statusStr = j.getStatus() != null ? j.getStatus().getLabel() : "Pendiente";
            tvStatus.setText(statusStr);

            if (showActions && j.getStatus() != null
                    && j.getStatus() == com.iepca.app.model.enums.JustificationStatus.PENDING) {
                layoutActions.setVisibility(View.VISIBLE);
                btnApprove.setOnClickListener(v -> {
                    if (listener != null) listener.onApprove(j);
                });
                btnReject.setOnClickListener(v -> {
                    if (listener != null) listener.onReject(j);
                });
            } else {
                layoutActions.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(j);
            });
        }

        private String formatDate(String raw) {
            if (raw == null) return "";
            return raw.length() >= 10 ? raw.substring(0, 10) : raw;
        }
    }
}
