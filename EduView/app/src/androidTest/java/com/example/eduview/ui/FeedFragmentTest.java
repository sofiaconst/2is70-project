package com.example.eduview;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eduview.ui.feed.FeedFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FeedFragmentTest {

    private FragmentScenario<FeedFragment> launch() {
        return FragmentScenario.launchInContainer(
                FeedFragment.class,
                null,
                androidx.appcompat.R.style.Theme_AppCompat
        );
    }

    @Test
    public void testLaunchFragment() {
        FragmentScenario<FeedFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
        });
    }

    @Test
    public void testReloadButtonClick() {
        FragmentScenario<FeedFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            try {
                if (fragment.getView() != null) {
                    fragment.getView()
                            .findViewById(R.id.btnReloadFeed)
                            .performClick();
                }
            } catch (Exception e) {
                fail("Crash on reload click");
            }
        });
    }

    @Test
    public void testRecreateFragment() {
        FragmentScenario<FeedFragment> scenario = launch();
        scenario.recreate();
    }

    @Test
    public void testViewAccess() {
        FragmentScenario<FeedFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            if (fragment.getView() != null) {
                fragment.getView().findViewById(R.id.viewPager);
                fragment.getView().findViewById(R.id.TeacherTabs);
            }
        });
    }
}