package com.example.eduview;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eduview.ui.feed.Posts;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Posts();
            case 1:
                return new com.example.eduview.Announcements();
            case 2:
                return new com.example.eduview.Pending();
            default:
                return new Posts();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
