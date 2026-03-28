package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eduview.R;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.example.eduview.ui.main.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Fragment for displaying the feed screen.
 * It initializes tabs, loads posts based on the user role,
 * and handles retrying when the user session is not ready.
 */
public class FeedFragment extends Fragment {

    private User user;

    private FloatingActionButton btnReloadFeed;

    // View Models
    private MainViewModel mainViewModel;
    private FeedViewModel feedViewModel;

    // Tab Logic
    private TabLayout teacherTabs;
    private ViewPager2 viewPager;

    // Retry Logic
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int retryCount = 0;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 200;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method for creating a new instance of FeedFragment.
     *
     * @return a new instance of FeedFragment
     */
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    /**
     * Loads previous state when the fragment is being created if its not null.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FeedFragment", "OnCreate");
    }

    /**
     * Inflates the layout and initializes ViewModels when the view is created.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FeedFragment", "Fragment created");

        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        // Initialize ViewModels
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        return view;
    }

    /**
     * Initializes UI components and sets listeners after the view has been created.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FeedFragment", "View is created");

        // Initialize UI elements to set up tabs and reload function
        teacherTabs = view.findViewById(R.id.TeacherTabs);
        viewPager = view.findViewById(R.id.viewPager);
        btnReloadFeed = view.findViewById(R.id.btnReloadFeed);

        // Listeners for the reload button
        btnReloadFeed.setOnClickListener(v -> {
            Log.d("FeedFragment", "Reload button clicked");
            
            // Start rotation animation
            Animation rotation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_refresh);
            v.startAnimation(rotation);

            // Reload feed data
            feedViewModel.reloadAll();
        });

        // Try to setup the feed
        trySetupFeed();
    }

    /**
     * Attempts to initialize the feed. If the user is not yet available,
     * retries up to MAX_RETRIES times.
     */
    private void trySetupFeed() {
        user = mainViewModel.getCurrentUser();

        // Retry if user is not loaded
        if (user == null) {
            retryCount++;
            Log.e("FeedFragment", "Current user is NULL, retry " + retryCount);

            if (retryCount < MAX_RETRIES) {
                handler.postDelayed(this::trySetupFeed, RETRY_DELAY_MS);
            }
            return;
        }

        Log.d("FeedFragment", "User received: " + user.getUserId());
        Log.d("FeedFragment", "User role: " + user.getRole());

        boolean isTeacher = user.getRole() == UserRole.TEACHER;

        // Setup adapter for tabs
        viewPager.setAdapter(new FeedTabViewAdapter(this, isTeacher));

        // Attach TabLayout to ViewPager
        new TabLayoutMediator(teacherTabs, viewPager, (tab, position) -> {
            if (position == 0) tab.setText(getString(R.string.feed_title_1));
            else if (position == 1) tab.setText(getString(R.string.feed_title_2));
            else if (position == 2 && isTeacher) tab.setText(getString(R.string.feed_title_3));
        }).attach();

        // Load feed data
        feedViewModel.loadPostsForUser(user);
        feedViewModel.loadPublishedPosts();
        feedViewModel.loadAnnouncements();

        if (isTeacher) {
            feedViewModel.loadPendingPosts();
        }

        Log.d("FeedFragment", "Loading feed for user");
    }

    /**
     * Cleans up pending handler callbacks to prevent memory leaks when the fragment view
     * is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}