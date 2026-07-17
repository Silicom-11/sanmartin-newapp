package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecyclerView Adapter for Attendance taking.
 * Each row shows a student name with P/F/T radio buttons.
 */
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    /** Notifies the host when attendance counters change. */
    public interface OnCountChangeListener {
        void onCountChanged(int total, int present);
    }

    private List<Student> students = new ArrayList<>();
    private final Map<String, String> attendanceMap = new HashMap<>();
    private OnCountChangeListener countListener;

    public void setOnCountChangeListener(OnCountChangeListener listener) {
        this.countListener = listener;
    }

    public void setStudents(List<Student> newStudents) {
        this.students = newStudents != null ? newStudents : new ArrayList<>();
        attendanceMap.clear();
        for (Student s : students) {
            attendanceMap.put(s.getId(), "present");
        }
        notifyDataSetChanged();
        notifyCounts();
    }

    /** Returns map of studentId -> status (present/absent/late) */
    public Map<String, String> getAttendanceMap() {
        return new HashMap<>(attendanceMap);
    }

    private void notifyCounts() {
        if (countListener == null) return;
        int present = 0;
        for (String status : attendanceMap.values()) {
            if ("present".equals(status)) present++;
        }
        countListener.onCountChanged(students.size(), present);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(students.get(position), position + 1);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNumber, tvName;
        private final RadioButton rbPresent, rbAbsent, rbLate;
        private final RadioGroup radioGroup;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvName = itemView.findViewById(R.id.tvName);
            rbPresent = itemView.findViewById(R.id.rbPresent);
            rbAbsent = itemView.findViewById(R.id.rbAbsent);
            rbLate = itemView.findViewById(R.id.rbLate);
            radioGroup = itemView.findViewById(R.id.radioGroup);
        }

        void bind(Student student, int number) {
            tvNumber.setText(String.valueOf(number));
            tvName.setText(student.getLastName() + ", " + student.getFirstName());

            // Default to present
            rbPresent.setChecked(true);

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String status;
                if (checkedId == R.id.rbAbsent) {
                    status = "absent";
                } else if (checkedId == R.id.rbLate) {
                    status = "late";
                } else {
                    status = "present";
                }
                attendanceMap.put(student.getId(), status);
                notifyCounts();
            });
        }
    }
}
