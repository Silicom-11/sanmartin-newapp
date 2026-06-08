package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView Adapter for Parent's children cards.
 */
public class ChildCardAdapter extends RecyclerView.Adapter<ChildCardAdapter.ViewHolder> {

    private List<Student> items = new ArrayList<>();
    private final Map<String, Double> attendanceRates = new HashMap<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Student child);
    }

    public ChildCardAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Student> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setAttendanceRate(String studentId, double rate) {
        if (studentId != null) {
            attendanceRates.put(studentId, rate);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_card, parent, false);
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
        private final TextView tvName, tvGrade, tvAttendance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvGrade = itemView.findViewById(R.id.tvGrade);
            tvAttendance = itemView.findViewById(R.id.tvAttendance);
        }

        void bind(Student child) {
            tvName.setText(child.getFirstName() + " " + child.getLastName());
            String grade = (child.getGradeLevel() != null ? child.getGradeLevel() : "")
                    + (child.getSection() != null ? " - " + child.getSection() : "");
            tvGrade.setText(cleanText(grade));

            Double rate = attendanceRates.get(child.getId());
            if (rate != null) {
                tvAttendance.setText(String.format(Locale.getDefault(), "Asistencia: %.0f%%", rate));
                int color = rate >= 90 ? R.color.success : rate >= 75 ? R.color.warning : R.color.error;
                tvAttendance.setTextColor(ContextCompat.getColor(itemView.getContext(), color));
            } else {
                tvAttendance.setText("Asistencia: calculando...");
                tvAttendance.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_secondary));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(child);
            });
        }

        private String cleanText(String value) {
            if (value == null) return "";
            return value
                    .replace("Ã¡", "a").replace("Ã©", "e").replace("Ã­", "i")
                    .replace("Ã³", "o").replace("Ãº", "u").replace("Ã±", "n")
                    .replace("Ã", "A").replace("Ã‰", "E").replace("Ã", "I")
                    .replace("Ã“", "O").replace("Ãš", "U").replace("Ã‘", "N")
                    .replace("Â°", "°");
        }
    }
}
