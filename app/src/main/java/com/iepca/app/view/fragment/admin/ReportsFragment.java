package com.iepca.app.view.fragment.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.ReportDao;
import com.iepca.app.dao.SystemDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.util.UIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(ReportsFragment.class);

    private BarChart chartWeeklyAttendance;
    private PieChart chartGrades;
    private HorizontalBarChart chartRisk;
    private TextView txtJavaResourcesStatus;
    private MaterialButton btnTestJavaResources;
    private MaterialButton btnExportExcel;
    private SystemDao systemDao;
    private ReportDao reportDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        systemDao = RetrofitClient.createService(requireContext(), SystemDao.class);
        reportDao = RetrofitClient.createService(requireContext(), ReportDao.class);

        chartWeeklyAttendance = view.findViewById(R.id.chartWeeklyAttendance);
        chartGrades = view.findViewById(R.id.chartGrades);
        chartRisk = view.findViewById(R.id.chartRisk);
        txtJavaResourcesStatus = view.findViewById(R.id.txtJavaResourcesStatus);
        btnTestJavaResources = view.findViewById(R.id.btnTestJavaResources);
        btnExportExcel = view.findViewById(R.id.btnExportExcel);

        setupAttendanceChart();
        setupGradesChart();
        setupRiskChart();

        btnTestJavaResources.setOnClickListener(v -> verifySystemServices());
        btnExportExcel.setOnClickListener(v -> downloadExcelReport());
    }

    private void verifySystemServices() {
        txtJavaResourcesStatus.setText("Verificando servicios del sistema...");
        btnTestJavaResources.setEnabled(false);

        systemDao.getResourcesStatus().enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiResponse<Map<String, Object>>> response) {
                if (!isAdded()) return;
                btnTestJavaResources.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    txtJavaResourcesStatus.setText("No se pudo verificar el estado del sistema. Revisa la conexion y permisos.");
                    return;
                }

                Map<String, Object> data = response.body().getData();
                String status = ""
                        + "Monitoreo: " + nested(data, "guava", "status")
                        + "\n   Ultima medicion: " + nested(data, "guava", "checkedAt")
                        + "\n\nReportes Excel: " + nested(data, "apachePoi", "status")
                        + "\n   Archivo institucional: " + nested(data, "apachePoi", "fileName")
                        + "\n\nValidacion de archivos: " + nested(data, "apacheCommons", "status")
                        + "\n   Archivo saneado: " + nested(data, "apacheCommons", "safeFileName")
                        + "\n\nAuditoria: " + nested(data, "logback", "status")
                        + "\n   Registro: " + nested(data, "logback", "logFile");
                txtJavaResourcesStatus.setText(status);
                LOG.info("System services status validated from mobile app");
                UIUtils.showToast(requireContext(), "Servicios verificados");
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Map<String, Object>>> call,
                                  @NonNull Throwable throwable) {
                if (!isAdded()) return;
                btnTestJavaResources.setEnabled(true);
                txtJavaResourcesStatus.setText("Error conectando al backend: " + throwable.getMessage());
                LOG.error("System services status failed", throwable);
            }
        });
    }

    private void downloadExcelReport() {
        txtJavaResourcesStatus.setText("Generando reporte Excel...");
        btnExportExcel.setEnabled(false);

        reportDao.downloadStudentsExcel().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;
                btnExportExcel.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    txtJavaResourcesStatus.setText("No se pudo descargar el Excel. Codigo HTTP: " + response.code());
                    return;
                }

                try {
                    File file = saveReport(response.body());
                    txtJavaResourcesStatus.setText("Reporte Excel generado correctamente.\nArchivo: "
                            + file.getAbsolutePath() + "\nTamano: " + file.length() + " bytes");
                    LOG.info("Excel report downloaded: {}", file.getAbsolutePath());
                    UIUtils.showToast(requireContext(), "Reporte Excel exportado");
                } catch (IOException exception) {
                    txtJavaResourcesStatus.setText("Error guardando Excel: " + exception.getMessage());
                    LOG.error("Could not save Excel report", exception);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded()) return;
                btnExportExcel.setEnabled(true);
                txtJavaResourcesStatus.setText("Error descargando Excel: " + throwable.getMessage());
                LOG.error("Excel download failed", throwable);
            }
        });
    }

    private File saveReport(ResponseBody body) throws IOException {
        File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) {
            directory = requireContext().getFilesDir();
        }
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("No se pudo crear carpeta de descargas");
        }

        File file = new File(directory, "estudiantes_iepca.xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(body.bytes());
        }
        return file;
    }

    @SuppressWarnings("unchecked")
    private String nested(Map<String, Object> data, String section, String key) {
        if (data == null || !(data.get(section) instanceof Map)) {
            return "-";
        }
        Object value = ((Map<String, Object>) data.get(section)).get(key);
        return value != null ? value.toString() : "-";
    }

    private void setupAttendanceChart() {
        String[] days = {"Lun", "Mar", "Mie", "Jue", "Vie"};
        List<BarEntry> entries = new ArrayList<>();
        float[] values = {94f, 91f, 96f, 93f, 89f};
        for (int i = 0; i < values.length; i++) {
            entries.add(new BarEntry(i, values[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "% Asistencia");
        dataSet.setColor(requireContext().getColor(R.color.primary));
        dataSet.setValueTextSize(11f);

        chartWeeklyAttendance.setData(new BarData(dataSet));
        chartWeeklyAttendance.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        chartWeeklyAttendance.getXAxis().setGranularity(1f);
        chartWeeklyAttendance.getDescription().setEnabled(false);
        chartWeeklyAttendance.getAxisLeft().setAxisMinimum(0f);
        chartWeeklyAttendance.getAxisLeft().setAxisMaximum(100f);
        chartWeeklyAttendance.getAxisRight().setEnabled(false);
        chartWeeklyAttendance.animateY(800);
        chartWeeklyAttendance.invalidate();
    }

    private void setupGradesChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(18f, "AD"));
        entries.add(new PieEntry(42f, "A"));
        entries.add(new PieEntry(28f, "B"));
        entries.add(new PieEntry(12f, "C"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                requireContext().getColor(R.color.grade_ad),
                requireContext().getColor(R.color.grade_a),
                requireContext().getColor(R.color.grade_b),
                requireContext().getColor(R.color.grade_c));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        chartGrades.setData(new PieData(dataSet));
        chartGrades.setUsePercentValues(true);
        chartGrades.getDescription().setEnabled(false);
        chartGrades.setCenterText("Notas");
        chartGrades.setHoleRadius(40f);
        chartGrades.animateY(800);
        chartGrades.invalidate();
    }

    private void setupRiskChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 70f));
        entries.add(new BarEntry(1f, 20f));
        entries.add(new BarEntry(2f, 10f));

        BarDataSet dataSet = new BarDataSet(entries, "Estudiantes %");
        dataSet.setColors(
                requireContext().getColor(R.color.risk_low),
                requireContext().getColor(R.color.risk_medium),
                requireContext().getColor(R.color.risk_high));
        dataSet.setValueTextSize(11f);

        String[] labels = {"Bajo", "Medio", "Alto"};
        chartRisk.setData(new BarData(dataSet));
        chartRisk.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartRisk.getXAxis().setGranularity(1f);
        chartRisk.getDescription().setEnabled(false);
        chartRisk.getAxisLeft().setAxisMinimum(0f);
        chartRisk.getAxisRight().setEnabled(false);
        chartRisk.animateX(800);
        chartRisk.invalidate();
    }
}
