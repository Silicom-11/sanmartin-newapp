package com.iepca.app.view.fragment.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.LocationDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;
import com.iepca.app.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTrackingFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng PICHANAKI = new LatLng(-10.9279, -74.8723);

    private Spinner spinnerStudent;
    private TextView tvStatus;
    private TextView tvLastUpdate;
    private TextView tvBattery;
    private TextView tvCoordinates;
    private TextView tvAccuracy;
    private MaterialButton btnRefreshGps;
    private GoogleMap googleMap;
    private LocationDao locationDao;
    private List<Location> studentLocations = new ArrayList<>();

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

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        updateEmptyState();
        loadStudentLocations();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PICHANAKI, 14f));
        showAllMarkers();
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
                    showAllMarkers();
                    updateSummaryForAll();
                    return;
                }
                Location loc = studentLocations.get(pos - 1);
                showSingleMarker(loc);
                updateSummaryForLocation(loc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showAllMarkers() {
        if (googleMap == null) return;
        googleMap.clear();
        if (studentLocations == null || studentLocations.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PICHANAKI, 14f));
            return;
        }

        LatLngBounds.Builder bounds = LatLngBounds.builder();
        int validMarkers = 0;
        for (Location loc : studentLocations) {
            if (!isValidLocation(loc)) continue;
            LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
            String title = loc.getStudent() != null ? loc.getStudent().getFullName() : "Alumno";
            googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(title)
                    .snippet(loc.isOnline() ? "GPS activo" : "Ultima ubicacion")
                    .icon(BitmapDescriptorFactory.defaultMarker(loc.isOnline()
                            ? BitmapDescriptorFactory.HUE_GREEN
                            : BitmapDescriptorFactory.HUE_ORANGE)));
            bounds.include(point);
            validMarkers++;
        }

        if (validMarkers == 1) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 15f));
        } else if (validMarkers > 1) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 90));
        }
    }

    private void showSingleMarker(Location loc) {
        if (googleMap == null || !isValidLocation(loc)) return;
        googleMap.clear();
        LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
        String title = loc.getStudent() != null ? loc.getStudent().getFullName() : "Ubicacion actual";
        googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title(title)
                .snippet(loc.isOnline() ? "En linea" : "Desconectado")
                .icon(BitmapDescriptorFactory.defaultMarker(loc.isOnline()
                        ? BitmapDescriptorFactory.HUE_GREEN
                        : BitmapDescriptorFactory.HUE_ORANGE)));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
    }

    private void updateSummaryForAll() {
        int total = studentLocations != null ? studentLocations.size() : 0;
        int online = 0;
        if (studentLocations != null) {
            for (Location loc : studentLocations) if (loc.isOnline()) online++;
        }
        tvStatus.setText("Online " + online + "/" + total);
        tvBattery.setText(total > 0 ? total + " GPS" : "--");
        tvLastUpdate.setText(total > 0 ? "Panel actualizado" : "--");
        tvCoordinates.setText("Coordenadas: " + total + " estudiantes monitoreados");
        tvAccuracy.setText("Precision: seleccione un estudiante para ver detalle");
    }

    private void updateSummaryForLocation(Location loc) {
        tvStatus.setText(loc.isOnline() ? "En linea" : "Offline");
        tvLastUpdate.setText(shortTimestamp(loc.getTimestamp()));
        tvBattery.setText((int) loc.getBattery() + "%");
        tvCoordinates.setText(String.format(Locale.US, "Coordenadas: %.5f, %.5f",
                loc.getLatitude(), loc.getLongitude()));
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
}
