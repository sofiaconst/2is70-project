package com.example.eduview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FeedFragment extends Fragment {

    TabLayout teacherTabs;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;

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
        Log.d("FeedFragment","OnCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FeedFragment","onCreateView is created");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FeedFragment","View is created");
        TabLayout teacherTabs = view.findViewById(R.id.TeacherTabs);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new ViewPagerAdapter(this));

        new TabLayoutMediator(teacherTabs, viewPager, (tab, position) -> {
            if (position == 0) tab.setText(getString(R.string.feed_title_1)); // Posts
            else if (position == 1) tab.setText(getString(R.string.feed_title_2)); // Announcements
            else tab.setText(getString(R.string.feed_title_3)); // Pending
        }).attach();
    }
}