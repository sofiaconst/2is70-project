package com.example.eduview.ui.feed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

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
                return new Announcements();
            case 2:
                return new Pending();
            default:
                return new Posts();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
