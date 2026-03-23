package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.eduview.data.model.Student;

import java.util.List;

public class ParentPostsPagerAdapter extends FragmentStateAdapter {

    private final List<Student> children;

    public ParentPostsPagerAdapter(@NonNull Fragment fragment, List<Student> children) {
        super(fragment);
        this.children = children;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Student child = children.get(position);
        Log.d("ParentPostsPager", "Tab " + position + " -> " + child.getFirstName() + " classId=" + child.getClassId());
        return ParentStudentPostsFragment.newInstance(child.getClassId());
    }

    @Override
    public int getItemCount() {
        return children.size();
    }
}