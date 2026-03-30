package com.example.eduview.ui;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.ui.profile.ProfileFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {

    private FragmentScenario<ProfileFragment> launch() {
        Parent testParent = new Parent(
                "1",
                "Test",
                "User",
                "test@test.com",
                new java.util.ArrayList<>()
        );

        // Must run on main thread because setCurrentUserForTest uses LiveData.setValue().
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            SessionManager.getInstance().setCurrentUserForTest(testParent);
        });

        return FragmentScenario.launchInContainer(
                ProfileFragment.class,
                null,
                R.style.Theme_EduView
        );
    }

    @Test
    public void testLaunchFragment() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
        });
    }

    @Test
    public void testLogoutClick() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            try {
                if (fragment.getView() != null &&
                        fragment.getView().findViewById(R.id.buttonLogout) != null) {
                    fragment.getView().findViewById(R.id.buttonLogout).performClick();
                }
            } catch (Exception e) {
                fail("Crash on logout");
            }
        });
    }

    @Test
    public void testEditProfilePicture() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            try {
                if (fragment.getView() != null &&
                        fragment.getView().findViewById(R.id.tvEditPfp) != null) {
                    fragment.getView().findViewById(R.id.tvEditPfp).performClick();
                }
            } catch (Exception e) {
                fail("Crash on edit pfp");
            }
        });
    }

    @Test
    public void testScanQRClick() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            try {
                if (fragment.getView() != null &&
                        fragment.getView().findViewById(R.id.buttonScanQR) != null) {
                    fragment.getView().findViewById(R.id.buttonScanQR).performClick();
                }
            } catch (Exception e) {
                fail("Crash on scan QR");
            }
        });
    }

    @Test
    public void testAddChildClick() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            try {
                if (fragment.getView() != null &&
                        fragment.getView().findViewById(R.id.btnAddChild) != null) {
                    fragment.getView().findViewById(R.id.btnAddChild).performClick();
                }
            } catch (Exception e) {
                fail("Crash on add child");
            }
        });
    }

    @Test
    public void testRecreate() {
        FragmentScenario<ProfileFragment> scenario = launch();
        scenario.recreate();
    }

    @Test
    public void testViewAccess() {
        FragmentScenario<ProfileFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            if (fragment.getView() != null) {
                fragment.getView().findViewById(R.id.User_name_text);
                fragment.getView().findViewById(R.id.textViewRole);
                fragment.getView().findViewById(R.id.profileImage);
            }
        });
    }
}