package com.example.eduview.ui.feed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FeedTabViewAdapter extends FragmentStateAdapter {

    private final boolean isTeacher;
    public FeedTabViewAdapter(@NonNull Fragment fragment, boolean isTeacher) {
        super(fragment);
        this.isTeacher = isTeacher;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new Posts();
        } else if (position == 1) {
            return new Announcements();
        } else if (position == 2 && isTeacher) {
            return new Pending();
        }

        return new Posts();
    }

    @Override
    public int getItemCount() {
        return isTeacher ? 3 : 2;
    }
}
