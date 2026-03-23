package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.ui.main.MainViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Posts extends Fragment {

    private RecyclerView recyclerPosts;
    private FeedAdapter feedAdapter;

    public Posts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        FeedViewModel feedViewModel = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);

        User currentUser = mainViewModel.getCurrentUser();
        if (currentUser == null) {
            Log.e("PostsFragment", "Current user is null");
            return;
        }

        Log.d("PostsFragment", "Current user role: " + currentUser.getRole());
        Log.d("PostsFragment", "Current user type: " + currentUser.getClass().getName());
        Log.d("PostsFragment", "Is parent instance: " + (currentUser instanceof Parent));

        if (currentUser instanceof Parent) {
            setupParentTabs(view, feedViewModel, (Parent) currentUser);
        } else {
            setupNormalPostsList(view, feedViewModel);
        }
    }

    private void setupNormalPostsList(@NonNull View view, @NonNull FeedViewModel feedViewModel) {
        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setVisibility(View.VISIBLE);

        feedAdapter = new FeedAdapter(feedViewModel);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(feedAdapter);

        View parentTabsContainer = view.findViewById(R.id.parentTabsContainer);
        if (parentTabsContainer != null) {
            parentTabsContainer.setVisibility(View.GONE);
        }

        observePublishedPosts(feedViewModel);
        feedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            observePublishedPosts(feedViewModel);
        });
    }

    private void observePublishedPosts(@NonNull FeedViewModel feedViewModel) {
        if (feedViewModel.getPublishedPosts() != null) {
            feedViewModel.getPublishedPosts().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    feedAdapter.setItems(items);
                }
            });
        }
    }

    private void setupParentTabs(@NonNull View view,
                                 @NonNull FeedViewModel feedViewModel,
                                 @NonNull Parent parent) {

        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setVisibility(View.GONE);

        View parentTabsContainer = view.findViewById(R.id.parentTabsContainer);
        parentTabsContainer.setVisibility(View.VISIBLE);

        TabLayout childTabs = view.findViewById(R.id.childTabs);
        ViewPager2 childViewPager = view.findViewById(R.id.childViewPager);

        feedViewModel.loadChildrenForParent(parent);

        feedViewModel.getParentChildren().observe(getViewLifecycleOwner(), children -> {
            if (children == null || children.isEmpty()) {
                Log.d("PostsFragment", "No parent children with valid classrooms");
                childTabs.setVisibility(View.GONE);
                childViewPager.setVisibility(View.GONE);
                return;
            }

            for (Student child : children) {
                Log.d("PostsFragment", "Child: "
                        + child.getFirstName()
                        + " | classId=" + child.getClassId()
                        + " | userId=" + child.getUserId());
            }

            childTabs.setVisibility(View.VISIBLE);
            childViewPager.setVisibility(View.VISIBLE);

            ParentPostsPagerAdapter adapter = new ParentPostsPagerAdapter(this, children);
            childViewPager.setAdapter(adapter);

            new TabLayoutMediator(childTabs, childViewPager, (tab, position) -> {
                Student child = children.get(position);
                tab.setText(child.getFirstName());
            }).attach();
        });
    }
}