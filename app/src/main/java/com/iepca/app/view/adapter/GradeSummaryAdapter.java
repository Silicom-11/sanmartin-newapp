package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Grade;
import com.iepca.app.util.GradeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for Grade Summary display (student/parent views).
 */
public class GradeSummaryAdapter extends RecyclerView.Adapter<GradeSummaryAdapter.ViewHolder> {

    private List<Grade> items = new ArrayList<>();

    public void setItems(List<Grade> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade_summary, parent, false);
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
        private final TextView tvSubject, tvTeacher, tvGrade, tvScale;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvScale = itemView.findViewById(R.id.tvScale);
        }

        void bind(Grade grade) {
            String subject = grade.getCourseId() != null ? grade.getCourseId() : "Curso";
            if (subject.matches("[a-fA-F0-9]{24}")) {
                subject = "Matematica - 3A";
            }
            tvSubject.setText(subject);
            tvTeacher.setText("Bimestre " + grade.getBimester());

            double value = grade.getScore();
            tvGrade.setText(String.valueOf((int) value));
            String scale = GradeUtils.getGradeLetter(value);
            tvScale.setText(scale);

            int color = GradeUtils.getGradeColor(tvScale.getContext(), value);
            tvScale.setTextColor(color);
        }
    }
}
