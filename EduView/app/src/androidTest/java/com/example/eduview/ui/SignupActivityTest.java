package com.example.eduview.ui;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eduview.R;
import com.example.eduview.ui.signup.SignupActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SignupActivityTest {

    // Test 1: launch activity (covers onCreate)
    @Test
    public void testLaunchActivity() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            assertNotNull(activity);
        });
    }

    // Test 2: click signup with empty inputs (hits validation branches)
    @Test
    public void testSignupEmptyFields() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            try {
                activity.findViewById(R.id.btn_signup).performClick();
            } catch (Exception e) {
                fail("Crash on empty signup");
            }
        });
    }

    // Test 3: fill fields with invalid email + weak password
    @Test
    public void testInvalidInput() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            try {
                EditText first = activity.findViewById(R.id.et_first_name);
                EditText last = activity.findViewById(R.id.et_last_name);
                EditText email = activity.findViewById(R.id.et_email);
                EditText password = activity.findViewById(R.id.et_password);

                first.setText("John");
                last.setText("Doe");
                email.setText("invalid"); // triggers email validation
                password.setText("123");  // triggers weak password

                activity.findViewById(R.id.btn_signup).performClick();

            } catch (Exception e) {
                fail("Crash on invalid input");
            }
        });
    }

    // Test 4: valid-looking input (forces deeper execution)
    @Test
    public void testValidLookingInput() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            try {
                EditText first = activity.findViewById(R.id.et_first_name);
                EditText last = activity.findViewById(R.id.et_last_name);
                EditText email = activity.findViewById(R.id.et_email);
                EditText password = activity.findViewById(R.id.et_password);

                first.setText("Jane");
                last.setText("Doe");
                email.setText("jane@test.com");
                password.setText("Password1!");

                activity.findViewById(R.id.btn_signup).performClick();

            } catch (Exception e) {
                fail("Crash on valid input");
            }
        });
    }

    // Test 5: switch between teacher and parent (covers fragment logic)
    @Test
    public void testSwitchRoles() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            try {
                activity.findViewById(R.id.btn_role_teacher).performClick();
                activity.findViewById(R.id.btn_role_parent).performClick();
            } catch (Exception e) {
                fail("Crash when switching roles");
            }
        });
    }

    // Test 6: click login navigation (covers bottom path)
    @Test
    public void testLoginRedirect() {
        ActivityScenario<SignupActivity> scenario =
                ActivityScenario.launch(SignupActivity.class);

        scenario.onActivity(activity -> {
            try {
                activity.findViewById(R.id.tv_login).performClick();
            } catch (Exception e) {
                fail("Crash on login click");
            }
        });
    }
}