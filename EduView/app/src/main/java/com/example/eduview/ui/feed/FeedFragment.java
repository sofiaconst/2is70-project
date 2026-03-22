package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eduview.R;
import com.example.eduview.data.model.User;
import com.example.eduview.data.model.UserRole;
import com.example.eduview.ui.main.MainViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FeedFragment extends Fragment {

    private MainViewModel mainViewModel;
    private FeedViewModel feedViewModel;
    private User user;

    private TabLayout teacherTabs;
    private ViewPager2 viewPager;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int retryCount = 0;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 200;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("FeedFragment", "OnCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FeedFragment", "Fragment created");

        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FeedFragment", "View is created");

        teacherTabs = view.findViewById(R.id.TeacherTabs);
        viewPager = view.findViewById(R.id.viewPager);

        trySetupFeed();
    }

    private void trySetupFeed() {
        user = mainViewModel.getCurrentUser();

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

        viewPager.setAdapter(new FeedTabViewAdapter(this, isTeacher));

        new TabLayoutMediator(teacherTabs, viewPager, (tab, position) -> {
            if (position == 0) tab.setText(getString(R.string.feed_title_1)); // Posts
            else if (position == 1) tab.setText(getString(R.string.feed_title_2)); // Announcements
            else if (position == 2 && isTeacher) tab.setText(getString(R.string.feed_title_3)); // Pending
        }).attach();

        feedViewModel.loadPostsForUser(user);
        feedViewModel.loadPublishedPosts();
        feedViewModel.loadAnnouncements();

        if (isTeacher) {
            feedViewModel.loadPendingPosts();
        }

        Log.d("FeedFragment", "Loading feed for user");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}