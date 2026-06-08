package com.iepca.app.view.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
 * RecyclerView Adapter for Grade entry form.
 */
public class GradeEntryAdapter extends RecyclerView.Adapter<GradeEntryAdapter.ViewHolder> {

    private List<Student> students = new ArrayList<>();
    private final Map<String, Double> gradesMap = new HashMap<>();

    public void setStudents(List<Student> newStudents) {
        this.students = newStudents != null ? newStudents : new ArrayList<>();
        gradesMap.clear();
        notifyDataSetChanged();
    }

    /** Returns map of studentId -> grade value */
    public Map<String, Double> getGradesMap() {
        return new HashMap<>(gradesMap);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade_row, parent, false);
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
        private final EditText etGrade;
        private TextWatcher watcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvName = itemView.findViewById(R.id.tvName);
            etGrade = itemView.findViewById(R.id.etGrade);
        }

        void bind(Student student, int number) {
            tvNumber.setText(String.valueOf(number));
            tvName.setText(student.getLastName() + ", " + student.getFirstName());

            // Remove previous watcher
            if (watcher != null) {
                etGrade.removeTextChangedListener(watcher);
            }

            // Set existing grade if any
            Double existing = gradesMap.get(student.getId());
            etGrade.setText(existing != null ? String.valueOf(existing) : "");

            watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double value = Double.parseDouble(s.toString());
                        if (value >= 0 && value <= 20) {
                            gradesMap.put(student.getId(), value);
                        }
                    } catch (NumberFormatException e) {
                        gradesMap.remove(student.getId());
                    }
                }
            };
            etGrade.addTextChangedListener(watcher);
        }
    }
}
