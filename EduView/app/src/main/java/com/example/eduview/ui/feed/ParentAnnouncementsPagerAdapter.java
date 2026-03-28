package com.example.eduview.ui.feed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eduview.data.model.Student;

import java.util.List;

/**
 * Adapter for managing announcement fragments for each child of a parent.
 * Each page represents announcements of one child's classroom.
 */
public class ParentAnnouncementsPagerAdapter extends FragmentStateAdapter {

    private final List<Student> children;

    /**
     * Creates the adapter with a list of children.
     *
     * @param fragment parent fragment hosting the ViewPager
     * @param children list of children to create tabs for
     */
    public ParentAnnouncementsPagerAdapter(@NonNull Fragment fragment, List<Student> children) {
        super(fragment);
        this.children = children;
    }

    /**
     * Creates a fragment for a child based on position.
     *
     * @param position index of the child
     * @return fragment displaying announcements for the child's classroom
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Student child = children.get(position);
        return ParentStudentAnnouncementsFragment.newInstance(child.getClassId());
    }

    /**
     * Returns the number of children.
     *
     * @return number of tabs
     */
    @Override
    public int getItemCount() {
        return children.size();
    }
}