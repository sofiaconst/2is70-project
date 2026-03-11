package com.example.eduview.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eduview.R;

import com.example.eduview.SignupActivity;
import com.example.eduview.ui.main.MainActivity;
import com.google.firebase.FirebaseApp;
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

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Handle System Bar Insets to avoid black screen/content overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvError = findViewById(R.id.tv_error);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvSignup = findViewById(R.id.tv_signup);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

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

        // Handles forgot password
        tvForgotPassword.setOnClickListener(v -> {
            // Creates an input box for popup
            EditText resetEmailInput = new EditText(this);
            resetEmailInput.setHint("Enter your email");
            resetEmailInput.setInputType(
                    // Sets input type to email
                    android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            );

            String currentEmail = etUsername.getText().toString().trim();
            if (!currentEmail.isEmpty()) {
                resetEmailInput.setText(currentEmail);
            }

            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            int padding = (int) (20 * getResources().getDisplayMetrics().density);
            container.setPadding(padding, padding / 2, padding, 0);

            container.addView(resetEmailInput);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Reset password")
                    .setMessage("Enter your email to receive a password reset link.")
                    .setView(container)
                    .setPositiveButton("Send", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();

            // Handles positive button click
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(sendView -> {
                String email = resetEmailInput.getText().toString().trim();

                resetEmailInput.setPadding(20,20,20,20);
                resetEmailInput.setTextSize(16);

                // Checks if email is empty
                if (email.isEmpty()) {
                    resetEmailInput.setError("Email is required.");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    resetEmailInput.setError("Enter a valid email address.");
                    return;
                }

                // Sends password reset email
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this,
                                        "If an account with that email exists, a reset link has been sent.",
                                        Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                String msg = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Failed to send reset email.";
                                resetEmailInput.setError(msg);
                            }
                        });
            });
        });

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}