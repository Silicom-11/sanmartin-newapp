package com.iepca.app.view.fragment.parent;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.Constants;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.LocationDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildrenLocationFragment extends Fragment {

    private static final GeoPoint PICHANAKI = new GeoPoint(Constants.SCHOOL_LAT, Constants.SCHOOL_LON);

    private Spinner spinnerChild;
    private TextView tvChildStatus;
    private TextView tvChildLastSeen;
    private TextView tvChildCoordinates;
    private TextView tvChildAccuracy;
    private TextView tvChildSafety;
    private MaterialButton btnRefreshChildLocation;
    private MapView mapView;
    private LocationDao locationDao;
    private StudentDao studentDao;
    private List<Student> children = new ArrayList<>();
    private String selectedChildId;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = () -> {
        if (selectedChildId != null) loadChildLocation(selectedChildId);
        scheduleRefresh();
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_children_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationDao = RetrofitClient.createService(requireContext(), LocationDao.class);
        studentDao = RetrofitClient.createService(requireContext(), StudentDao.class);

        spinnerChild = view.findViewById(R.id.spinnerChild);
        tvChildStatus = view.findViewById(R.id.tvChildStatus);
        tvChildLastSeen = view.findViewById(R.id.tvChildLastSeen);
        tvChildCoordinates = view.findViewById(R.id.tvChildCoordinates);
        tvChildAccuracy = view.findViewById(R.id.tvChildAccuracy);
        tvChildSafety = view.findViewById(R.id.tvChildSafety);
        btnRefreshChildLocation = view.findViewById(R.id.btnRefreshChildLocation);
        btnRefreshChildLocation.setOnClickListener(v -> {
            if (selectedChildId != null) loadChildLocation(selectedChildId);
        });

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(PICHANAKI);

        updateEmptyState();
        loadChildren();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        scheduleRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void scheduleRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, Constants.GPS_MAP_REFRESH_MS);
    }

    private void loadChildren() {
        studentDao.getMyChildren().enqueue(new Callback<ApiResponse<List<Student>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Student>>> call,
                                   @NonNull Response<ApiResponse<List<Student>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    children = response.body().getData();
                    if (children == null) children = new ArrayList<>();
                    populateChildrenSpinner();
                } else {
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Student>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                updateEmptyState();
                UIUtils.showToast(requireContext(), "Error cargando estudiantes");
            }
        });
    }

    private void populateChildrenSpinner() {
        List<String> names = new ArrayList<>();
        for (Student child : children) {
            names.add(child.getFullName());
        }
        if (names.isEmpty()) {
            names.add("Sin estudiantes vinculados");
        }
        spinnerChild.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, names));
        spinnerChild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (children == null || children.isEmpty()) {
                    updateEmptyState();
                    return;
                }
                selectedChildId = children.get(pos).getId();
                loadChildLocation(selectedChildId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadChildLocation(String childId) {
        locationDao.getChildLocation(childId).enqueue(new Callback<ApiResponse<Location>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Location>> call,
                                   @NonNull Response<ApiResponse<Location>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Location loc = response.body().getData();
                    if (loc != null && isValidLocation(loc)) {
                        renderLocation(loc);
                    } else {
                        updateEmptyState();
                    }
                } else {
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Location>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                updateEmptyState();
                UIUtils.showToast(requireContext(), "Error cargando ubicacion");
            }
        });
    }

    private int getMarkerColor(Location loc) {
        if (!loc.isOnline()) return requireContext().getColor(R.color.warning);
        if (Boolean.FALSE.equals(loc.getInsidePerimeter())) return requireContext().getColor(R.color.error);
        return requireContext().getColor(R.color.success);
    }

    private void renderLocation(Location loc) {
        if (mapView != null) {
            mapView.getOverlays().clear();
            GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());

            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(loc.getStudent() != null ? loc.getStudent().getFullName() : "Estudiante");

            String snippet;
            if (!loc.isOnline()) {
                snippet = "Offline";
            } else if (Boolean.FALSE.equals(loc.getInsidePerimeter())) {
                snippet = "FUERA del perimetro escolar";
            } else {
                snippet = "Dentro del perimetro escolar";
            }
            marker.setSnippet(snippet);

            Drawable icon = ContextCompat.getDrawable(requireContext(),
                    org.osmdroid.library.R.drawable.marker_default);
            if (icon != null) {
                Drawable tinted = DrawableCompat.wrap(icon.mutate());
                DrawableCompat.setTint(tinted, getMarkerColor(loc));
                marker.setIcon(tinted);
            }
            marker.showInfoWindow();

            mapView.getOverlays().add(marker);
            mapView.getController().setZoom(17.0);
            mapView.getController().animateTo(point);
            mapView.invalidate();
        }

        tvChildStatus.setText(loc.isOnline() ? "Estado: en linea" : "Estado: desconectado");
        tvChildLastSeen.setText("Ultima conexion: " + shortTimestamp(loc.getTimestamp()));
        tvChildCoordinates.setText(String.format(Locale.US, "Coordenadas: %.5f, %.5f",
                loc.getLatitude(), loc.getLongitude()));

        String accuracyText = String.format(Locale.US, "Precision GPS: %.1f m", loc.getAccuracy());
        if (loc.getDistanceToSchool() != null) {
            accuracyText += String.format(Locale.US, " | Dist. colegio: %.0fm", loc.getDistanceToSchool());
        }
        tvChildAccuracy.setText(accuracyText);

        tvChildSafety.setText(getSemaforoText(loc));
    }

    private String getSemaforoText(Location loc) {
        if (!loc.isOnline()) {
            return "Semaforo GPS: AMARILLO - sin conexion, revisar ultima ubicacion";
        }
        if (Boolean.FALSE.equals(loc.getInsidePerimeter())) {
            String text = "Semaforo GPS: ROJO - FUERA del perimetro escolar";
            if (loc.getDistanceToSchool() != null) {
                text += String.format(Locale.US, " (%.0fm del colegio)", loc.getDistanceToSchool());
            }
            return text;
        }
        return "Semaforo GPS: VERDE - dentro del perimetro escolar";
    }

    private void updateEmptyState() {
        tvChildStatus.setText("Estado: --");
        tvChildLastSeen.setText("Ultima conexion: --");
        tvChildCoordinates.setText("Coordenadas: esperando senal GPS");
        tvChildAccuracy.setText("Precision GPS: --");
        tvChildSafety.setText("Semaforo GPS: esperando ubicacion");
        if (mapView != null) {
            mapView.getOverlays().clear();
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(PICHANAKI);
            mapView.invalidate();
        }
    }

    private boolean isValidLocation(Location loc) {
        return loc != null
                && (Math.abs(loc.getLatitude()) > 0.0001 || Math.abs(loc.getLongitude()) > 0.0001);
    }

    private String shortTimestamp(String timestamp) {
        if (timestamp == null || timestamp.equals("--")) return "--";
        return timestamp.length() > 19 ? timestamp.substring(0, 19).replace('T', ' ') : timestamp;
    }
}
