package com.iepca.app.view.fragment.admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;
import com.iepca.app.util.UIUtils;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTrackingFragment extends Fragment {

    private static final GeoPoint PICHANAKI = new GeoPoint(Constants.SCHOOL_LAT, Constants.SCHOOL_LON);

    private Spinner spinnerStudent;
    private TextView tvStatus;
    private TextView tvLastUpdate;
    private TextView tvBattery;
    private TextView tvCoordinates;
    private TextView tvAccuracy;
    private MaterialButton btnRefreshGps;
    private MaterialButton btnOpenGoogleMaps;
    private MapView mapView;
    private LocationDao locationDao;
    private List<Location> studentLocations = new ArrayList<>();
    private Location selectedLocation;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = () -> {
        loadStudentLocations();
        scheduleRefresh();
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gps_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationDao = RetrofitClient.createService(requireContext(), LocationDao.class);

        spinnerStudent = view.findViewById(R.id.spinnerStudent);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvLastUpdate = view.findViewById(R.id.tvLastUpdate);
        tvBattery = view.findViewById(R.id.tvBattery);
        tvCoordinates = view.findViewById(R.id.tvCoordinates);
        tvAccuracy = view.findViewById(R.id.tvAccuracy);
        btnRefreshGps = view.findViewById(R.id.btnRefreshGps);
        btnRefreshGps.setOnClickListener(v -> loadStudentLocations());
        btnOpenGoogleMaps = view.findViewById(R.id.btnOpenGoogleMaps);
        btnOpenGoogleMaps.setOnClickListener(v -> openInGoogleMaps());

        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(PICHANAKI);

        updateEmptyState();
        loadStudentLocations();
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

    private void loadStudentLocations() {
        locationDao.getStudentLocations().enqueue(new Callback<ApiResponse<List<Location>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Location>>> call,
                                   @NonNull Response<ApiResponse<List<Location>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    studentLocations = response.body().getData();
                    if (studentLocations == null) studentLocations = new ArrayList<>();
                    populateSpinner();
                    showAllMarkers();
                    updateSummaryForAll();
                } else {
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Location>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                updateEmptyState();
                UIUtils.showToast(requireContext(), "Error cargando ubicaciones");
            }
        });
    }

    private void populateSpinner() {
        List<String> names = new ArrayList<>();
        names.add("Todos los estudiantes");
        for (Location loc : studentLocations) {
            String name = loc.getStudent() != null ? loc.getStudent().getFullName() : "Estudiante";
            names.add(name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
        spinnerStudent.setAdapter(adapter);
        spinnerStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (pos == 0) {
                    selectedLocation = null;
                    btnOpenGoogleMaps.setVisibility(View.GONE);
                    showAllMarkers();
                    updateSummaryForAll();
                    return;
                }
                Location loc = studentLocations.get(pos - 1);
                selectedLocation = loc;
                btnOpenGoogleMaps.setVisibility(View.VISIBLE);
                showSingleMarker(loc);
                updateSummaryForLocation(loc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int getMarkerColor(Location loc) {
        if (!loc.isOnline()) return requireContext().getColor(R.color.warning);
        if (Boolean.FALSE.equals(loc.getInsidePerimeter())) return requireContext().getColor(R.color.error);
        return requireContext().getColor(R.color.success);
    }

    private Marker buildMarker(Location loc, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);

        String snippet;
        if (!loc.isOnline()) {
            snippet = "Offline";
        } else if (Boolean.FALSE.equals(loc.getInsidePerimeter())) {
            snippet = "FUERA del perimetro";
        } else {
            snippet = "Dentro del perimetro";
        }
        if (loc.getDistanceToSchool() != null) {
            snippet += String.format(Locale.US, " (%.0fm)", loc.getDistanceToSchool());
        }
        marker.setSnippet(snippet);

        Drawable icon = ContextCompat.getDrawable(requireContext(),
                org.osmdroid.library.R.drawable.marker_default);
        if (icon != null) {
            Drawable tinted = DrawableCompat.wrap(icon.mutate());
            DrawableCompat.setTint(tinted, getMarkerColor(loc));
            marker.setIcon(tinted);
        }
        return marker;
    }

    private Polygon buildSchoolPerimeter() {
        Polygon circle = new Polygon(mapView);
        circle.setPoints(Polygon.pointsAsCircle(PICHANAKI, Constants.SCHOOL_RADIUS_M));
        circle.setStrokeColor(Color.argb(180, 33, 150, 243));
        circle.setStrokeWidth(2f);
        circle.setFillColor(Color.argb(40, 33, 150, 243));
        circle.setTitle("IEP Continental Americano");
        circle.setSnippet("Perimetro escolar (" + (int) Constants.SCHOOL_RADIUS_M + "m)");
        return circle;
    }

    private void showAllMarkers() {
        if (mapView == null) return;
        mapView.getOverlays().clear();
        mapView.getOverlays().add(buildSchoolPerimeter());

        if (studentLocations == null || studentLocations.isEmpty()) {
            mapView.getController().setZoom(15.0);
            mapView.getController().setCenter(PICHANAKI);
            mapView.invalidate();
            return;
        }

        List<GeoPoint> points = new ArrayList<>();
        for (Location loc : studentLocations) {
            if (!isValidLocation(loc)) continue;
            String title = loc.getStudent() != null ? loc.getStudent().getFullName() : "Alumno";
            mapView.getOverlays().add(buildMarker(loc, title));
            points.add(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        }

        if (points.size() == 1) {
            mapView.getController().setZoom(16.0);
            mapView.getController().animateTo(points.get(0));
        } else if (points.size() > 1) {
            BoundingBox box = BoundingBox.fromGeoPoints(points);
            mapView.post(() -> mapView.zoomToBoundingBox(box.increaseByScale(1.4f), true));
        }
        mapView.invalidate();
    }

    private void showSingleMarker(Location loc) {
        if (mapView == null || !isValidLocation(loc)) return;
        mapView.getOverlays().clear();
        mapView.getOverlays().add(buildSchoolPerimeter());
        String title = loc.getStudent() != null ? loc.getStudent().getFullName() : "Ubicacion actual";
        Marker marker = buildMarker(loc, title);
        marker.showInfoWindow();
        mapView.getOverlays().add(marker);
        mapView.getController().setZoom(17.0);
        mapView.getController().animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        mapView.invalidate();
    }

    private void updateSummaryForAll() {
        int total = studentLocations != null ? studentLocations.size() : 0;
        int online = 0;
        int inside = 0;
        int outside = 0;
        if (studentLocations != null) {
            for (Location loc : studentLocations) {
                if (loc.isOnline()) online++;
                if (Boolean.TRUE.equals(loc.getInsidePerimeter())) inside++;
                if (Boolean.FALSE.equals(loc.getInsidePerimeter())) outside++;
            }
        }
        tvStatus.setText("Online " + online + "/" + total
                + " | Perimetro: " + inside + " dentro, " + outside + " fuera");
        tvBattery.setText(total > 0 ? total + " GPS" : "--");
        tvLastUpdate.setText(total > 0 ? "Auto-refresh cada 30s" : "--");
        tvCoordinates.setText("Coordenadas: " + total + " estudiantes monitoreados");
        tvAccuracy.setText("Seleccione un estudiante para ver detalle");
    }

    private void updateSummaryForLocation(Location loc) {
        String status = loc.isOnline() ? "En linea" : "Offline";
        if (loc.isOnline() && Boolean.FALSE.equals(loc.getInsidePerimeter())) {
            status += " - FUERA del perimetro";
        } else if (loc.isOnline() && Boolean.TRUE.equals(loc.getInsidePerimeter())) {
            status += " - Dentro del perimetro";
        }
        tvStatus.setText(status);
        tvLastUpdate.setText(shortTimestamp(loc.getTimestamp()));
        tvBattery.setText((int) loc.getBattery() + "%");

        String coords = String.format(Locale.US, "Coordenadas: %.5f, %.5f",
                loc.getLatitude(), loc.getLongitude());
        if (loc.getDistanceToSchool() != null) {
            coords += String.format(Locale.US, " | Dist: %.0fm", loc.getDistanceToSchool());
        }
        tvCoordinates.setText(coords);
        tvAccuracy.setText(String.format(Locale.US, "Precision: %.1f m | Velocidad: %.1f m/s",
                loc.getAccuracy(), loc.getSpeed()));
    }

    private void updateEmptyState() {
        tvStatus.setText("Sin datos");
        tvLastUpdate.setText("--");
        tvBattery.setText("--");
        tvCoordinates.setText("Coordenadas: esperando senal GPS");
        tvAccuracy.setText("Precision: --");
    }

    private boolean isValidLocation(Location loc) {
        return loc != null
                && (Math.abs(loc.getLatitude()) > 0.0001 || Math.abs(loc.getLongitude()) > 0.0001);
    }

    private String shortTimestamp(String timestamp) {
        if (timestamp == null || timestamp.equals("--")) return "--";
        return timestamp.length() > 19 ? timestamp.substring(0, 19).replace('T', ' ') : timestamp;
    }

    private void openInGoogleMaps() {
        if (selectedLocation == null || !isValidLocation(selectedLocation)) {
            UIUtils.showToast(requireContext(), "Seleccione un estudiante con ubicacion");
            return;
        }
        String name = selectedLocation.getStudent() != null
                ? selectedLocation.getStudent().getFullName() : "Estudiante";
        String uri = String.format(Locale.US, "geo:0,0?q=%.6f,%.6f(%s)",
                selectedLocation.getLatitude(), selectedLocation.getLongitude(),
                Uri.encode(name));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String webUrl = String.format(Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f",
                    selectedLocation.getLatitude(), selectedLocation.getLongitude());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
        }
    }
}
