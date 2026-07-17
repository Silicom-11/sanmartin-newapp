package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Attendance;
import com.iepca.app.model.Student;
import com.iepca.app.model.enums.AttendanceStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only list of attendance records for the admin view.
 * Resolves student names through a directory of known students.
 */
public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder> {

    private List<Attendance> items = new ArrayList<>();
    private Map<String, Student> studentDirectory = new HashMap<>();

    public void setItems(List<Attendance> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setStudentDirectory(Map<String, Student> directory) {
        this.studentDirectory = directory != null ? directory : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_record, parent, false);
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
        private final TextView tvName, tvDetail, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Attendance a) {
            Student student = a.getStudentId() != null ? studentDirectory.get(a.getStudentId()) : null;
            tvName.setText(student != null
                    ? student.getLastName() + ", " + student.getFirstName()
                    : "Estudiante " + shortId(a.getStudentId()));

            String arrival = a.getArrivalTime() != null && !a.getArrivalTime().isEmpty()
                    ? "Hora de llegada: " + a.getArrivalTime() : "Sin hora registrada";
            tvDetail.setText(arrival);

            AttendanceStatus status = a.getStatus();
            int color;
            String label;
            if (status == null) {
                color = R.color.text_hint; label = "—";
            } else {
                switch (status) {
                    case PRESENT: color = R.color.status_present; label = "PRESENTE"; break;
                    case LATE: color = R.color.status_late; label = "TARDANZA"; break;
                    case JUSTIFIED: color = R.color.status_justified; label = "JUSTIFICADO"; break;
                    default: color = R.color.status_absent; label = "AUSENTE"; break;
                }
            }
            tvStatus.setText(label);
            tvStatus.getBackground().mutate().setTint(itemView.getContext().getColor(color));
        }

        private String shortId(String id) {
            if (id == null) return "";
            return id.length() > 6 ? id.substring(id.length() - 6) : id;
        }
    }
}
