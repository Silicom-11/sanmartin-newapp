package com.iepca.app.view.fragment.parent;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.iepca.app.R;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.dao.LocationDao;
import com.iepca.app.dao.StudentDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.model.Location;
import com.iepca.app.model.Student;
import com.iepca.app.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChildrenLocationFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng PICHANAKI = new LatLng(-10.9279, -74.8723);

    private Spinner spinnerChild;
    private TextView tvChildStatus;
    private TextView tvChildLastSeen;
    private TextView tvChildCoordinates;
    private TextView tvChildAccuracy;
    private TextView tvChildSafety;
    private MaterialButton btnRefreshChildLocation;
    private GoogleMap googleMap;
    private LocationDao locationDao;
    private StudentDao studentDao;
    private List<Student> children = new ArrayList<>();
    private String selectedChildId;

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

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        updateEmptyState();
        loadChildren();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PICHANAKI, 14f));
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

    private void renderLocation(Location loc) {
        if (googleMap != null) {
            googleMap.clear();
            LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(loc.getStudent() != null ? loc.getStudent().getFullName() : "Estudiante")
                    .snippet(loc.isOnline() ? "GPS activo" : "Ultima ubicacion")
                    .icon(BitmapDescriptorFactory.defaultMarker(loc.isOnline()
                            ? BitmapDescriptorFactory.HUE_GREEN
                            : BitmapDescriptorFactory.HUE_ORANGE)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16f));
        }

        tvChildStatus.setText(loc.isOnline() ? "Estado: en linea" : "Estado: desconectado");
        tvChildLastSeen.setText("Ultima conexion: " + shortTimestamp(loc.getTimestamp()));
        tvChildCoordinates.setText(String.format(Locale.US, "Coordenadas: %.5f, %.5f",
                loc.getLatitude(), loc.getLongitude()));
        tvChildAccuracy.setText(String.format(Locale.US, "Precision GPS: %.1f m", loc.getAccuracy()));
        tvChildSafety.setText(loc.isOnline()
                ? "Semaforo GPS: VERDE - ubicacion actualizada"
                : "Semaforo GPS: AMARILLO - revisar ultima conexion");
    }

    private void updateEmptyState() {
        tvChildStatus.setText("Estado: --");
        tvChildLastSeen.setText("Ultima conexion: --");
        tvChildCoordinates.setText("Coordenadas: esperando senal GPS");
        tvChildAccuracy.setText("Precision GPS: --");
        tvChildSafety.setText("Semaforo GPS: esperando ubicacion");
        if (googleMap != null) {
            googleMap.clear();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PICHANAKI, 14f));
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
