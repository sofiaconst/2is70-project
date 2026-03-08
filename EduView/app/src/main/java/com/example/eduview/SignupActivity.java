package com.example.eduview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentSignupFragment()).commit();
        }

        // Possible way of displaying account created view?
//        Button buttonSignUp = findViewById(R.id.btn_signup);
//        LinearLayout success = findViewById(R.id.signup_success_container);
//
//        buttonSignUp.setOnClickListener(v -> {
//            success.setVisibility(View.VISIBLE);
//        });
    }
}