package com.example.eduview.ui.main;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.eduview.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflate layout and apply edge-to-edge insets
        setupLayout();

        // 2. Initialize MainViewModel and session
        setupViewModel();
    }

    /** Layout and padding setup */
    private void setupLayout() {
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );
    }

    /** Initialize ViewModel and start session */
    private void setupViewModel() {
        // Get ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Start session asynchronously and provide a callback for when user info is ready
        mainViewModel.startSession(this::onSessionReady);
    }

    /** Callback invoked once the session/user info is ready */
    private void onSessionReady() {
        // Safe: user info exists, now we can set up navigation
        setupNavigation();
    }
    /** Setup bottom navigation and NavController */
    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.MainBottomNavigationView);
        bottomNav.getMenu().clear();

        // Ask ViewModel for the menu resource ID (decouples Activity from user/role logic)
        int menuRes = mainViewModel.getMenuResForUser();
        bottomNav.inflateMenu(menuRes);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_container);

        if (navHostFragment == null) {
            Log.e("NAV", "NavHostFragment not found!");
            return;
        }

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    public MainViewModel getMainViewModel() {
        return this.mainViewModel;
    }

}