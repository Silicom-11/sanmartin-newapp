package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Teacher;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for Teachers list.
 */
public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.ViewHolder> {

    private List<Teacher> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Teacher teacher);
    }

    public TeacherAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Teacher> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
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
        private final TextView tvName, tvSpecialty;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
        }

        void bind(Teacher teacher) {
            tvName.setText(teacher.getFirstName() + " " + teacher.getLastName());
            tvSpecialty.setText(teacher.getSpecialty() != null ? teacher.getSpecialty() : "Sin especialidad");
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(teacher);
            });
        }
    }
}
