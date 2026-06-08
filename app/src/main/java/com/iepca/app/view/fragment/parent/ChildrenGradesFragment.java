package com.iepca.app.view.fragment.parent;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.ParentDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildrenGradesFragment extends Fragment {

    private Spinner spinnerChild;
    private TextView tvStudentSubtitle;
    private TextView tvAverage;
    private TextView tvAverageLabel;
    private TextView tvAttendance;
    private TextView tvAttendanceLabel;
    private TextView tvKpiStatus;
    private TextView tvKpiDetail;
    private TextView tvEmptyGrades;
    private LinearLayout gradesContainer;
    private LinearLayout attendanceContainer;

    private StudentDao studentDao;
    private ParentDao parentDao;
    private final List<Student> children = new ArrayList<>();
    private final List<Map<String, Object>> grades = new ArrayList<>();
    private final List<Map<String, Object>> attendance = new ArrayList<>();
    private final DecimalFormat oneDecimal = new DecimalFormat("0.0");

    private MaterialButton[] periodButtons;
    private int selectedPeriod = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_children_grades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);
        parentDao = RetrofitClient.createService(requireContext(), ParentDao.class);

        bindViews(view);
        configurePeriodButtons(view);
        showLoadingState();
        loadChildren();
    }

    private void bindViews(View view) {
        spinnerChild = view.findViewById(R.id.spinnerChild);
        tvStudentSubtitle = view.findViewById(R.id.tvStudentSubtitle);
        tvAverage = view.findViewById(R.id.tvAverage);
        tvAverageLabel = view.findViewById(R.id.tvAverageLabel);
        tvAttendance = view.findViewById(R.id.tvAttendance);
        tvAttendanceLabel = view.findViewById(R.id.tvAttendanceLabel);
        tvKpiStatus = view.findViewById(R.id.tvKpiStatus);
        tvKpiDetail = view.findViewById(R.id.tvKpiDetail);
        tvEmptyGrades = view.findViewById(R.id.tvEmptyGrades);
        gradesContainer = view.findViewById(R.id.gradesContainer);
        attendanceContainer = view.findViewById(R.id.attendanceContainer);
    }

    private void configurePeriodButtons(View view) {
        periodButtons = new MaterialButton[] {
                view.findViewById(R.id.btnPeriodAll),
                view.findViewById(R.id.btnPeriod1),
                view.findViewById(R.id.btnPeriod2),
                view.findViewById(R.id.btnPeriod3),
                view.findViewById(R.id.btnPeriod4)
        };

        for (int i = 0; i < periodButtons.length; i++) {
            final int period = i;
            periodButtons[i].setOnClickListener(v -> {
                selectedPeriod = period;
                updatePeriodButtons();
                renderGrades();
                updateSummary();
            });
        }
        updatePeriodButtons();
    }

    private void updatePeriodButtons() {
        int primary = color(R.color.primary);
        int surface = color(R.color.surface);
        int stroke = color(R.color.card_stroke);

        for (int i = 0; i < periodButtons.length; i++) {
            MaterialButton button = periodButtons[i];
            boolean active = i == selectedPeriod;
            button.setBackgroundTintList(ColorStateList.valueOf(active ? primary : surface));
            button.setTextColor(active ? Color.WHITE : primary);
            button.setStrokeColor(ColorStateList.valueOf(active ? primary : stroke));
            button.setStrokeWidth(dp(1));
        }
    }

    private void loadChildren() {
        studentDao.getMyChildren().enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    children.clear();
                    if (response.body().getData() != null) {
                        children.addAll(response.body().getData());
                    }
                    bindChildren();
                } else {
                    showEmptyChildren();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                UIUtils.showToast(requireContext(), "Error cargando estudiantes");
                showEmptyChildren();
            }
        });
    }

    private void bindChildren() {
        if (children.isEmpty()) {
            showEmptyChildren();
            return;
        }

        List<String> names = new ArrayList<>();
        for (Student s : children) {
            names.add(s.getFullName());
        }

        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
        );
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChild.setAdapter(childAdapter);
        spinnerChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Student selected = children.get(position);
                tvStudentSubtitle.setText(String.format(Locale.getDefault(), "%s - Seccion %s",
                        cleanText(safe(selected.getGradeLevel(), "Grado no definido")),
                        cleanText(safe(selected.getSection(), "-"))));
                loadChildData(selected.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed.
            }
        });
    }

    private void loadChildData(String childId) {
        showLoadingState();
        loadChildGrades(childId);
        loadChildAttendance(childId);
    }

    private void loadChildGrades(String childId) {
        parentDao.getChildGrades(childId).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (!isAdded()) return;
                grades.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    grades.addAll(response.body().getData());
                }
                renderGrades();
                updateSummary();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                grades.clear();
                renderGrades();
                updateSummary();
                UIUtils.showToast(requireContext(), "Error cargando notas");
            }
        });
    }

    private void loadChildAttendance(String childId) {
        parentDao.getChildAttendance(childId).enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (!isAdded()) return;
                attendance.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    attendance.addAll(response.body().getData());
                }
                renderAttendance();
                updateSummary();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                attendance.clear();
                renderAttendance();
                updateSummary();
            }
        });
    }

    private void showLoadingState() {
        tvAverage.setText("--");
        tvAverageLabel.setText("Cargando...");
        tvAttendance.setText("--");
        tvAttendanceLabel.setText("Cargando...");
        tvKpiStatus.setText("Semaforo: --");
        tvKpiDetail.setText("Calculando rendimiento academico y asistencia.");
        gradesContainer.removeAllViews();
        attendanceContainer.removeAllViews();
        tvEmptyGrades.setVisibility(View.GONE);
    }

    private void showEmptyChildren() {
        tvStudentSubtitle.setText("No hay estudiantes vinculados a esta cuenta.");
        gradesContainer.removeAllViews();
        attendanceContainer.removeAllViews();
        tvEmptyGrades.setVisibility(View.VISIBLE);
        tvAverage.setText("--");
        tvAttendance.setText("--");
        tvKpiStatus.setText("Semaforo: sin datos");
        tvKpiDetail.setText("El administrador debe vincular estudiantes al padre de familia.");
    }

    private void renderGrades() {
        gradesContainer.removeAllViews();

        int shown = 0;
        for (Map<String, Object> grade : grades) {
            int bimester = asInt(grade.get("bimester"), 0);
            if (selectedPeriod != 0 && bimester != selectedPeriod) {
                continue;
            }
            addGradeCard(grade, shown);
            shown++;
        }

        tvEmptyGrades.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
    }

    private void addGradeCard(Map<String, Object> grade, int index) {
        String courseName = firstText(grade, "courseName", "courseLabel");
        if (courseName.isEmpty()) {
            courseName = sampleCourseName(index);
        }
        int bimester = asInt(grade.get("bimester"), 0);
        double average = asDouble(grade.get("average"), calculateAverageFromScores(grade));
        String status = safe(asString(grade.get("status")), "abierto");

        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(color(R.color.surface));
        card.setRadius(dp(18));
        card.setCardElevation(dp(1));
        card.setStrokeColor(color(R.color.card_stroke));
        card.setStrokeWidth(dp(1));

        LinearLayout body = new LinearLayout(requireContext());
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(16), dp(16), dp(14));
        card.addView(body);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        body.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView badge = new TextView(requireContext());
        badge.setText(courseInitial(courseName));
        badge.setGravity(android.view.Gravity.CENTER);
        badge.setTextColor(color(R.color.primary));
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setTextSize(18);
        badge.setBackgroundResource(R.drawable.bg_circle_icon);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        badgeParams.setMargins(0, 0, dp(12), 0);
        header.addView(badge, badgeParams);

        LinearLayout titleCol = new LinearLayout(requireContext());
        titleCol.setOrientation(LinearLayout.VERTICAL);
        header.addView(titleCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView title = new TextView(requireContext());
        title.setText(courseName);
        title.setTextColor(color(R.color.ink));
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        titleCol.addView(title);

        TextView subtitle = new TextView(requireContext());
        subtitle.setText(String.format(Locale.getDefault(), "Bimestre %d - %s", bimester, statusLabel(status)));
        subtitle.setTextColor(color(R.color.text_secondary));
        subtitle.setTextSize(13);
        titleCol.addView(subtitle);

        TextView score = new TextView(requireContext());
        score.setText(oneDecimal.format(average));
        score.setTextColor(gradeColor(average));
        score.setTextSize(24);
        score.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(score);

        addScoresDetail(body, grade);
        gradesContainer.addView(card);
    }

    @SuppressWarnings("unchecked")
    private void addScoresDetail(LinearLayout body, Map<String, Object> grade) {
        Object scoresObj = grade.get("scores");
        if (!(scoresObj instanceof List)) {
            return;
        }

        List<?> scores = (List<?>) scoresObj;
        if (scores.isEmpty()) {
            return;
        }

        View divider = new View(requireContext());
        divider.setBackgroundColor(color(R.color.divider));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        );
        dividerParams.setMargins(0, dp(14), 0, dp(10));
        body.addView(divider, dividerParams);

        for (int i = 0; i < scores.size(); i++) {
            Object entry = scores.get(i);
            double value = 0;
            if (entry instanceof Map) {
                value = asDouble(((Map<String, Object>) entry).get("score"), 0);
            } else {
                value = asDouble(entry, 0);
            }

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));
            body.addView(row);

            TextView label = new TextView(requireContext());
            label.setText(evaluationName(i));
            label.setTextColor(color(R.color.text_secondary));
            label.setTextSize(14);
            row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView valueText = new TextView(requireContext());
            valueText.setText(String.valueOf((int) value));
            valueText.setTextColor(gradeColor(value));
            valueText.setTextSize(16);
            valueText.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(valueText);
        }
    }

    private void renderAttendance() {
        attendanceContainer.removeAllViews();
        if (attendance.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No hay asistencias registradas.");
            empty.setTextColor(color(R.color.text_secondary));
            empty.setTextSize(14);
            empty.setPadding(0, dp(8), 0, 0);
            attendanceContainer.addView(empty);
            return;
        }

        int limit = Math.min(attendance.size(), 5);
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = attendance.get(i);
            String status = safe(asString(item.get("status")), "present");
            addAttendanceRow(item, status);
        }
    }

    private void addAttendanceRow(Map<String, Object> item, String status) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        attendanceContainer.addView(row);

        TextView dot = new TextView(requireContext());
        dot.setText(" ");
        dot.setBackgroundColor(attendanceColor(status));
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(10), dp(10));
        dotParams.setMargins(0, 0, dp(12), 0);
        row.addView(dot, dotParams);

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        row.addView(textCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView date = new TextView(requireContext());
        date.setText(formatDate(asString(item.get("date"))));
        date.setTextColor(color(R.color.ink));
        date.setTextSize(14);
        date.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(date);

        TextView detail = new TextView(requireContext());
        detail.setText(statusLabel(status));
        detail.setTextColor(attendanceColor(status));
        detail.setTextSize(13);
        detail.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(detail);
    }

    private void updateSummary() {
        double average = filteredAverage();
        double attendanceRate = attendanceRate();

        if (average > 0) {
            tvAverage.setText(oneDecimal.format(average));
            tvAverage.setTextColor(gradeColor(average));
            tvAverageLabel.setText(average >= 14 ? "Buen rendimiento" : "Requiere refuerzo");
        } else {
            tvAverage.setText("--");
            tvAverageLabel.setText("Sin datos");
            tvAverage.setTextColor(color(R.color.primary));
        }

        if (!attendance.isEmpty()) {
            tvAttendance.setText(String.format(Locale.getDefault(), "%.0f%%", attendanceRate));
            tvAttendance.setTextColor(attendanceRate >= 90 ? color(R.color.success)
                    : attendanceRate >= 75 ? color(R.color.warning) : color(R.color.error));
            tvAttendanceLabel.setText(attendance.size() + " registros");
        } else {
            tvAttendance.setText("--");
            tvAttendanceLabel.setText("Sin registros");
            tvAttendance.setTextColor(color(R.color.success));
        }

        updateSemaphore(average, attendanceRate);
    }

    private void updateSemaphore(double average, double attendanceRate) {
        int bg;
        String label;
        String detail;

        if (average >= 14 && attendanceRate >= 90) {
            bg = color(R.color.success);
            label = "Semaforo: VERDE";
            detail = "Rendimiento estable. El estudiante mantiene buen promedio y asistencia.";
        } else if (average >= 11 && attendanceRate >= 75) {
            bg = color(R.color.warning);
            label = "Semaforo: AMARILLO";
            detail = "Atencion preventiva. Se recomienda reforzar cursos y revisar tardanzas.";
        } else if (average > 0 || !attendance.isEmpty()) {
            bg = color(R.color.error);
            label = "Semaforo: ROJO";
            detail = "Riesgo academico. El tutor debe intervenir por bajo rendimiento o inasistencias.";
        } else {
            bg = color(R.color.primary);
            label = "Semaforo: sin datos";
            detail = "El indicador se activa cuando existan notas y asistencias registradas.";
        }

        tvKpiStatus.setText(label);
        tvKpiStatus.getBackground().mutate().setTint(bg);
        tvKpiDetail.setText(detail);
    }

    private double filteredAverage() {
        double sum = 0;
        int count = 0;
        for (Map<String, Object> grade : grades) {
            int bimester = asInt(grade.get("bimester"), 0);
            if (selectedPeriod != 0 && bimester != selectedPeriod) {
                continue;
            }
            double value = asDouble(grade.get("average"), calculateAverageFromScores(grade));
            if (value > 0) {
                sum += value;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private double attendanceRate() {
        if (attendance.isEmpty()) {
            return 0;
        }
        int ok = 0;
        for (Map<String, Object> item : attendance) {
            String status = asString(item.get("status")).toLowerCase(Locale.ROOT);
            if ("present".equals(status) || "justified".equals(status)) {
                ok++;
            }
        }
        return ok * 100.0 / attendance.size();
    }

    @SuppressWarnings("unchecked")
    private double calculateAverageFromScores(Map<String, Object> grade) {
        Object scoresObj = grade.get("scores");
        if (!(scoresObj instanceof List)) {
            return 0;
        }

        double sum = 0;
        int count = 0;
        for (Object entry : (List<?>) scoresObj) {
            double score = 0;
            if (entry instanceof Map) {
                score = asDouble(((Map<String, Object>) entry).get("score"), 0);
            } else {
                score = asDouble(entry, 0);
            }
            if (score > 0) {
                sum += score;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private String firstText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            String value = asString(map.get(key));
            if (!value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String sampleCourseName(int index) {
        String[] names = {
                "Matematica - 3 Secundaria A",
                "Comunicacion - 3 Secundaria A",
                "Ciencia y Tecnologia - 3 Secundaria A",
                "Historia - 3 Secundaria A"
        };
        return names[index % names.length];
    }

    private String evaluationName(int index) {
        String[] names = {
                "Practica calificada",
                "Tarea academica",
                "Examen bimestral",
                "Participacion",
                "Proyecto"
        };
        return names[index % names.length];
    }

    private String courseInitial(String courseName) {
        String trimmed = courseName == null ? "" : courseName.trim();
        if (trimmed.isEmpty()) return "C";
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private int gradeColor(double grade) {
        if (grade >= 17) return color(R.color.success);
        if (grade >= 14) return color(R.color.primary_variant);
        if (grade >= 11) return color(R.color.warning);
        return color(R.color.error);
    }

    private int attendanceColor(String status) {
        switch (status.toLowerCase(Locale.ROOT)) {
            case "absent":
                return color(R.color.error);
            case "late":
                return color(R.color.warning);
            case "justified":
                return color(R.color.info);
            case "present":
            default:
                return color(R.color.success);
        }
    }

    private String statusLabel(String status) {
        switch (status.toLowerCase(Locale.ROOT)) {
            case "closed":
            case "cerrado":
                return "cerrado";
            case "published":
            case "publicado":
                return "publicado";
            case "absent":
                return "Ausente";
            case "late":
                return "Tardanza";
            case "justified":
                return "Justificado";
            case "present":
                return "Presente";
            default:
                return status;
        }
    }

    private String formatDate(String raw) {
        if (raw == null || raw.length() < 10) {
            return "Fecha no registrada";
        }
        return raw.substring(0, 10);
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(String.valueOf(value)) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value != null ? Double.parseDouble(String.valueOf(value)) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : cleanText(String.valueOf(value));
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String cleanText(String value) {
        if (value == null) return "";
        return value
                .replace("Ã¡", "a").replace("Ã©", "e").replace("Ã­", "i")
                .replace("Ã³", "o").replace("Ãº", "u").replace("Ã±", "n")
                .replace("Ã", "A").replace("Ã‰", "E").replace("Ã", "I")
                .replace("Ã“", "O").replace("Ãš", "U").replace("Ã‘", "N")
                .replace("Â°", "")
                .replace("ÃƒÂ¡", "a").replace("ÃƒÂ©", "e").replace("ÃƒÂ­", "i")
                .replace("ÃƒÂ³", "o").replace("ÃƒÂº", "u").replace("ÃƒÂ±", "n")
                .replace("ÃƒÂ", "A").replace("ÃƒÂ‰", "E").replace("ÃƒÂ", "I")
                .replace("ÃƒÂ“", "O").replace("ÃƒÂš", "U").replace("ÃƒÂ‘", "N");
    }

    private int color(int resId) {
        return ContextCompat.getColor(requireContext(), resId);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
