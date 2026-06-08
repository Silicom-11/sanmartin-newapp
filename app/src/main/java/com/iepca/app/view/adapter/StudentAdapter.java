package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Student;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView Adapter for Students list.
 * Implements ViewHolder pattern following MVC architecture.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Student student);
    }

    public StudentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Student> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = items.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDetails, tvStatus;
        private final CircleImageView imgAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }

        void bind(Student student) {
            String fullName = student.getFirstName() + " " + student.getLastName();
            tvName.setText(fullName);

            String details = "DNI: " + (student.getDni() != null ? student.getDni() : "--");
            if (student.getGradeLevel() != null) {
                details += " | " + student.getGradeLevel();
            }
            if (student.getSection() != null) {
                details += " " + student.getSection();
            }
            tvDetails.setText(details);

            boolean active = "activo".equalsIgnoreCase(student.getStatus());
            tvStatus.setText(active ? "Activo" : "Inactivo");
            tvStatus.setBackgroundResource(active
                    ? R.drawable.bg_status_present : R.drawable.bg_status_absent);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(student);
            });
        }
    }
}
