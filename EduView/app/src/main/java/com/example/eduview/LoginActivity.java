package com.example.eduview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvSignup = findViewById(R.id.tv_signup);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                tvError.setText("Please fill in all fields.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if user exists in the database
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        databaseReference.child(userId).get().addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful() && dbTask.getResult().exists()) {
                                tvError.setVisibility(View.GONE);
                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                tvError.setText("User not found in the database.");
                                tvError.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        tvError.setText("Invalid credentials. Please try again.");
                        tvError.setVisibility(View.VISIBLE);
                    }
                });
        });

        tvSignup.setOnClickListener(v -> {
            // Navigate to SignupActivity
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
