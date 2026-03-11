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
import com.example.eduview.data.model.Student;
import com.example.eduview.data.repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        setupNavigation();

        // ---- Test UserRepository here ----
        //testUserRepository();

        setupViewModel();

    }

    private void setupViewModel() {

        MainViewModel viewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.loadCurrentUser();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.MainBottomNavigationView);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_container);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNav,navController);
    }

    private void setupLayout() {
        EdgeToEdge.enable(this); // app content can extend behind the status bar and navigation bar, modern feel
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void testUserRepository() {
        // Hardcoded test user
        String testUserId = "student_1";

        UserRepository userRepository = new UserRepository();

        userRepository.fetchUser_alt(
                testUserId,
                user -> {
                    // Success callback
                    Log.d("UserRepositoryTest", "User fetched successfully!");
                    Log.d("UserRepositoryTest", "ID: " + user.getUserId());
                    Log.d("UserRepositoryTest", "Name: " + user.getFirstName() + " " + user.getLastName());
                    Log.d("UserRepositoryTest", "Role: " + user.getRole());

                    if (user instanceof Student) {
                        Student s = (Student) user;
                        Log.d("UserRepositoryTest", "Classroom: " + s.getClassId());
                    }
                },
                error -> {
                    // Error callback
                    Log.e("UserRepositoryTest", "Failed to fetch user", error);
                }
        );
    }
}