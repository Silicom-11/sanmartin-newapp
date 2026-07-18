package com.iepca.app.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.iepca.app.R;
import com.iepca.app.config.Constants;
import com.iepca.app.config.RetrofitClient;
import com.iepca.app.config.SessionManager;
import com.iepca.app.databinding.ActivityMainBinding;
import com.iepca.app.view.fragment.admin.AdminDashboardFragment;
import com.iepca.app.view.fragment.admin.StudentsManagementFragment;
import com.iepca.app.view.fragment.admin.TeachersManagementFragment;
import com.iepca.app.view.fragment.admin.ParentsManagementFragment;
import com.iepca.app.view.fragment.admin.CoursesManagementFragment;
import com.iepca.app.view.fragment.admin.GradesManagementFragment;
import com.iepca.app.view.fragment.admin.AttendanceViewFragment;
import com.iepca.app.view.fragment.admin.JustificationsManagementFragment;
import com.iepca.app.view.fragment.admin.GPSTrackingFragment;
import com.iepca.app.view.fragment.admin.ReportsFragment;
import com.iepca.app.view.fragment.admin.SettingsFragment;
import com.iepca.app.view.fragment.teacher.TeacherDashboardFragment;
import com.iepca.app.view.fragment.teacher.GradesFormFragment;
import com.iepca.app.view.fragment.teacher.AttendanceFragment;
import com.iepca.app.view.fragment.parent.ParentDashboardFragment;
import com.iepca.app.view.fragment.parent.ChildrenGradesFragment;
import com.iepca.app.view.fragment.parent.JustificationFormFragment;
import com.iepca.app.view.fragment.parent.ChildrenLocationFragment;
import com.iepca.app.view.fragment.student.StudentDashboardFragment;
import com.iepca.app.view.fragment.student.StudentGradesFragment;
import com.iepca.app.view.fragment.shared.CalendarFragment;
import com.iepca.app.view.fragment.shared.ProfileFragment;
import com.iepca.app.view.fragment.shared.NotificationsFragment;
import com.iepca.app.dao.LocationDao;
import com.iepca.app.model.ApiResponse;
import com.iepca.app.service.LocationTrackingService;
import com.iepca.app.util.UIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main Activity - acts as the Navigation Controller (MVC pattern).
 * Hosts a DrawerLayout (admin side menu) + BottomNavigationView + Fragment container.
 * Dynamically configures navigation based on user role.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQ_LOCATION_TRACKING = 901;
    private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);
    private ActivityMainBinding binding;
    private SessionManager session;
    private String currentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = SessionManager.getInstance(this);
        currentRole = session.getUserRole();

        LOG.info("MainActivity created for role: {}", currentRole);

        setupToolbar();
        setupNavigation();

        if (savedInstanceState == null) {
            loadDefaultFragment();
        }

        maybeStartStudentLocationTracking();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Drawer toggle for admin
        if (session.isAdmin()) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, binding.drawerLayout, binding.toolbar,
                    R.string.nav_home, R.string.nav_home);
            binding.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            binding.navDrawer.setNavigationItemSelectedListener(this);
            populateDrawerHeader();
        } else {
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void populateDrawerHeader() {
        android.view.View header = binding.navDrawer.getHeaderView(0);
        if (header == null) return;
        android.widget.TextView tvUserName = header.findViewById(R.id.tvUserName);
        android.widget.TextView tvUserRole = header.findViewById(R.id.tvUserRole);
        if (tvUserName != null) tvUserName.setText(session.getUserName());
        if (tvUserRole != null) {
            String role = session.getUserRole();
            if (role != null && !role.isEmpty()) {
                tvUserRole.setText(role.substring(0, 1).toUpperCase() + role.substring(1));
            }
        }
    }

    private void setupNavigation() {
        // Configure bottom navigation based on role
        switch (currentRole) {
            case Constants.ROLE_ADMIN:
            case Constants.ROLE_DIRECTOR:
                binding.bottomNav.inflateMenu(R.menu.bottom_nav_admin);
                break;
            case Constants.ROLE_TEACHER:
                binding.bottomNav.inflateMenu(R.menu.bottom_nav_teacher);
                break;
            case Constants.ROLE_PARENT:
                binding.bottomNav.inflateMenu(R.menu.bottom_nav_parent);
                break;
            case Constants.ROLE_STUDENT:
                binding.bottomNav.inflateMenu(R.menu.bottom_nav_student);
                break;
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = getFragmentForNavItem(id);
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private Fragment getFragmentForNavItem(int itemId) {
        if (itemId == R.id.nav_home) return getDashboardFragment();
        if (itemId == R.id.nav_calendar) return new CalendarFragment();
        if (itemId == R.id.nav_notifications) return new NotificationsFragment();
        if (itemId == R.id.nav_profile) return new ProfileFragment();
        if (itemId == R.id.nav_grades) return getGradesFragment();
        if (itemId == R.id.nav_attendance) return getAttendanceFragment();
        if (itemId == R.id.nav_students) return new StudentsManagementFragment();
        if (itemId == R.id.nav_gps) return getGPSFragment();
        if (itemId == R.id.nav_justifications) return getJustificationsFragment();
        if (itemId == R.id.nav_settings) return new SettingsFragment();
        return null;
    }

    private Fragment getDashboardFragment() {
        switch (currentRole) {
            case Constants.ROLE_ADMIN:
            case Constants.ROLE_DIRECTOR: return new AdminDashboardFragment();
            case Constants.ROLE_TEACHER: return new TeacherDashboardFragment();
            case Constants.ROLE_PARENT: return new ParentDashboardFragment();
            case Constants.ROLE_STUDENT: return new StudentDashboardFragment();
            default: return new AdminDashboardFragment();
        }
    }

    private Fragment getGradesFragment() {
        switch (currentRole) {
            case Constants.ROLE_ADMIN:
            case Constants.ROLE_DIRECTOR: return new GradesManagementFragment();
            case Constants.ROLE_TEACHER: return new GradesFormFragment();
            case Constants.ROLE_PARENT: return new ChildrenGradesFragment();
            case Constants.ROLE_STUDENT: return new StudentGradesFragment();
            default: return new GradesManagementFragment();
        }
    }

    private Fragment getAttendanceFragment() {
        if (session.isTeacher()) return new AttendanceFragment();
        return new AttendanceViewFragment();
    }

    private Fragment getJustificationsFragment() {
        if (session.isParent()) return new JustificationFormFragment();
        return new JustificationsManagementFragment();
    }

    private Fragment getGPSFragment() {
        if (session.isParent()) return new ChildrenLocationFragment();
        return new GPSTrackingFragment();
    }

    private void loadDefaultFragment() {
        loadFragment(getDashboardFragment());
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void loadFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void selectBottomNavItem(int itemId) {
        binding.bottomNav.setSelectedItemId(itemId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;

        // Admin drawer navigation
        if (id == R.id.drawer_dashboard) fragment = new AdminDashboardFragment();
        else if (id == R.id.drawer_students) fragment = new StudentsManagementFragment();
        else if (id == R.id.drawer_teachers) fragment = new TeachersManagementFragment();
        else if (id == R.id.drawer_parents) fragment = new ParentsManagementFragment();
        else if (id == R.id.drawer_courses) fragment = new CoursesManagementFragment();
        else if (id == R.id.drawer_grades) fragment = new GradesManagementFragment();
        else if (id == R.id.drawer_attendance) fragment = new AttendanceViewFragment();
        else if (id == R.id.drawer_justifications) fragment = new JustificationsManagementFragment();
        else if (id == R.id.drawer_gps) fragment = new GPSTrackingFragment();
        else if (id == R.id.drawer_reports) fragment = new ReportsFragment();
        else if (id == R.id.drawer_settings) fragment = new SettingsFragment();
        else if (id == R.id.drawer_notifications) fragment = new NotificationsFragment();
        else if (id == R.id.drawer_logout) {
            logout();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        if (session.isStudent()) {
            try {
                LocationDao locationDao = RetrofitClient.createService(this, LocationDao.class);
                Map<String, Object> data = new HashMap<>();
                data.put("sessionStatus", "offline");
                locationDao.logoutLocation(data).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> c, Response<ApiResponse<Void>> r) {
                        LOG.info("GPS logout notificado al backend");
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                        LOG.warn("No se pudo notificar GPS logout: {}", t.getMessage());
                    }
                });
            } catch (Exception e) {
                LOG.warn("Error notificando GPS logout: {}", e.getMessage());
            }
        }
        stopService(new Intent(this, LocationTrackingService.class));
        session.clearSession();
        RetrofitClient.resetClient();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void maybeStartStudentLocationTracking() {
        if (!session.isStudent()) {
            stopService(new Intent(this, LocationTrackingService.class));
            return;
        }

        if (hasLocationPermissions()) {
            startLocationTrackingService();
            return;
        }

        ActivityCompat.requestPermissions(this, getRequiredLocationPermissions(), REQ_LOCATION_TRACKING);
    }

    private boolean hasLocationPermissions() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        return (fine || coarse) && notifications;
    }

    private String[] getRequiredLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        }
        return new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    private void startLocationTrackingService() {
        Intent intent = new Intent(this, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
        } else {
            startService(intent);
        }
        LOG.info("Student GPS tracking service requested");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_TRACKING) {
            if (hasLocationPermissions()) {
                startLocationTrackingService();
                UIUtils.showToast(this, "Seguimiento GPS activado");
            } else {
                UIUtils.showToast(this, "Activa permisos de ubicacion para usar GPS");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
