package com.example.eduview.ui.feed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eduview.data.model.Student;

import java.util.List;

public class ParentAnnouncementsPagerAdapter extends FragmentStateAdapter {

    private final List<Student> children;

    public ParentAnnouncementsPagerAdapter(@NonNull Fragment fragment, List<Student> children) {
        super(fragment);
        this.children = children;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Student child = children.get(position);
        return ParentStudentAnnouncementsFragment.newInstance(child.getClassId());
    }

    @Override
    public int getItemCount() {
        return children.size();
    }
}