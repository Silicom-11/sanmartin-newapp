package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for Courses list.
 */
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public CourseAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Course> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
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
        private final TextView tvName, tvDetails, tvSchedule;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
        }

        void bind(Course course) {
            tvName.setText(course.getName() != null ? course.getName() : "Sin nombre");
            tvDetails.setText(course.getGradeLevel() != null ? course.getGradeLevel() : "");
            tvSchedule.setText(course.getSection() != null ? course.getSection() : "");
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(course);
            });
        }
    }
}
