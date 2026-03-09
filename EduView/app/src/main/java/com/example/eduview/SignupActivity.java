package com.example.eduview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SignupActivity", "onCreate: SignupActivity started");
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_signup);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } catch (Exception e) {
            Log.e("SignupActivity", "Error during initialization", e);
        }

        Button buttonTeacher = findViewById(R.id.btn_role_teacher);

        buttonTeacher.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new TeacherSignupFragment()).commit();
        });

        Button buttonParent = findViewById(R.id.btn_role_parent);

        buttonParent.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
        });

        Button btnSignUp = findViewById(R.id.btn_signup);
        EditText etFirstName = findViewById(R.id.et_first_name);
        EditText etLastName = findViewById(R.id.et_last_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);

        btnSignUp.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullName = firstName + " " + lastName;

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

                        Log.d("SignupActivity", "Attempting to write user data to Firebase: " + userId);

                        databaseReference.child(userId).setValue(new User(fullName, email))
                            .addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Log.d("SignupActivity", "User data successfully written to Firebase.");
                                    Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    Log.e("SignupActivity", "Failed to save user data to Firebase", dbTask.getException());
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                    } else {
                        Log.e("SignupActivity", "Sign Up Failed", task.getException());
                        Toast.makeText(this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        TextView tvLogin = findViewById(R.id.tv_login);
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    public static class User {
        public String name;
        public String email;

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
