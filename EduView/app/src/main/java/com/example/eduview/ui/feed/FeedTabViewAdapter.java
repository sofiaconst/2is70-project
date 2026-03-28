package com.example.eduview.ui.feed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter responsible for adding the tabs to the tab layout in feed fragment.
 */
public class FeedTabViewAdapter extends FragmentStateAdapter {

    private final boolean isTeacher;

    /**
     * Creates a new FeedTabViewAdapter with boolean isTeacher to see whether the user is a teacher.
     *
     * @param fragment fragment the tabs are in
     * @param isTeacher whether the user is a teacher
     */
    public FeedTabViewAdapter(@NonNull Fragment fragment, boolean isTeacher) {
        super(fragment);
        this.isTeacher = isTeacher;
    }

    /**
     * Creates the tabs in the feed fragment.
     *
     * @param position position of the tab names
     * @return the fragment that should be opened according to the position
     */
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

    /**
     * Returns the number of tabs according to the role
     * @return the number of tabs in feed fragment
     */
    @Override
    public int getItemCount() {
        return isTeacher ? 3 : 2;
    }
}
