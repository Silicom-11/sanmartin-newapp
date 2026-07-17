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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView Adapter for Grade Summary display.
 *
 * Course and student ids coming from the API are resolved to real names
 * through the optional directories set by the host screen.
 */
public class GradeSummaryAdapter extends RecyclerView.Adapter<GradeSummaryAdapter.ViewHolder> {

    private List<Grade> items = new ArrayList<>();
    private Map<String, String> courseNames = new HashMap<>();
    private Map<String, String> studentNames = new HashMap<>();

    public void setItems(List<Grade> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** courseId -> course display name */
    public void setCourseNames(Map<String, String> names) {
        this.courseNames = names != null ? names : new HashMap<>();
        notifyDataSetChanged();
    }

    /** studentId -> student display name (used in the admin view) */
    public void setStudentNames(Map<String, String> names) {
        this.studentNames = names != null ? names : new HashMap<>();
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
            tvSubject.setText(resolveCourse(grade.getCourseId()));

            String studentName = grade.getStudentId() != null
                    ? studentNames.get(grade.getStudentId()) : null;
            String detail = "Bimestre " + grade.getBimester();
            if (studentName != null) {
                detail = studentName + " — " + detail;
            }
            tvTeacher.setText(detail);

            double value = grade.getScore();
            tvGrade.setText(String.valueOf((int) value));
            String scale = GradeUtils.getGradeLetter(value);
            tvScale.setText(scale);

            int color = GradeUtils.getGradeColor(tvScale.getContext(), value);
            tvScale.setTextColor(color);
        }

        private String resolveCourse(String courseId) {
            if (courseId == null) return "Curso";
            String known = courseNames.get(courseId);
            if (known != null) return known;
            if (courseId.matches("[a-fA-F0-9]{24}")) {
                return "Curso " + courseId.substring(courseId.length() - 4);
            }
            return courseId;
        }
    }
}
