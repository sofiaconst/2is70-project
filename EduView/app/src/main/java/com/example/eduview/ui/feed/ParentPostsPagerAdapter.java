package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eduview.data.model.Student;

import java.util.List;

/**
 * Adapter for managing post fragments for each child of a parent.
 * Each page represents posts of one child's classroom.
 */
public class ParentPostsPagerAdapter extends FragmentStateAdapter {

    private final List<Student> children;

    /**
     * Creates the adapter with a list of children.
     *
     * @param fragment parent fragment hosting the ViewPager
     * @param children list of children to create tabs for
     */
    public ParentPostsPagerAdapter(@NonNull Fragment fragment, List<Student> children) {
        super(fragment);
        this.children = children;
    }

    /**
     * Creates a fragment for a child based on position.
     *
     * @param position index of the child
     * @return fragment displaying posts for the child's classroom
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Student child = children.get(position);
        Log.d("ParentPostsPager", "Tab " + position + " -> " + child.getFirstName() + " classId=" + child.getClassId());
        return ParentStudentPostsFragment.newInstance(child.getClassId());
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