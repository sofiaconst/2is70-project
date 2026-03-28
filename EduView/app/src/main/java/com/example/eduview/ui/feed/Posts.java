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

/**
 * Fragment for displaying published posts.
 * Shows a standard post list for teachers and students, and child-specific tabs for parents.
 */
public class Posts extends Fragment {

    // Feed helpers to create the view for user
    private RecyclerView recyclerPosts;
    private FeedAdapter feedAdapter;

    /**
     * Default constructor.
     */
    public Posts() {
        // Required empty public constructor
    }

    /**
     * Inflates the posts fragment layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return inflated fragment view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    /**
     * Initializes the appropriate UI depending on the current user's role.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
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

        // Set up parent tabs or normal posts list according to the role
        if (currentUser instanceof Parent) {
            setupParentTabs(view, feedViewModel, (Parent) currentUser);
        } else {
            setupNormalPostsList(view, feedViewModel);
        }
    }

    /**
     * Sets up the standard published-posts list for students and teachers.
     *
     * @param view fragment root view
     * @param feedViewModel shared feed ViewModel
     */
    private void setupNormalPostsList(@NonNull View view, @NonNull FeedViewModel feedViewModel) {
        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);
        View emptyPostsState = view.findViewById(R.id.emptyPostsState);

        recyclerPosts.setVisibility(View.VISIBLE);

        feedAdapter = new FeedAdapter(feedViewModel);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(feedAdapter);

        View parentTabsContainer = view.findViewById(R.id.parentTabsContainer);
        if (parentTabsContainer != null) {
            parentTabsContainer.setVisibility(View.GONE);
        }

        observePublishedPosts(feedViewModel, recyclerPosts, emptyPostsState);
        feedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            observePublishedPosts(feedViewModel, recyclerPosts, emptyPostsState);
        });
    }

    /**
     * Observes published posts and updates the list or empty state.
     *
     * @param feedViewModel shared feed ViewModel
     * @param recyclerPosts RecyclerView displaying posts
     * @param emptyPostsState view shown when there are no posts
     */
    private void observePublishedPosts(@NonNull FeedViewModel feedViewModel,
                                       @NonNull RecyclerView recyclerPosts,
                                       @NonNull View emptyPostsState) {
        if (feedViewModel.getPublishedPosts() != null) {
            feedViewModel.getPublishedPosts().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    feedAdapter.setItems(items);

                    if (items.isEmpty()) {
                        recyclerPosts.setVisibility(View.GONE);
                        emptyPostsState.setVisibility(View.VISIBLE);
                    } else {
                        recyclerPosts.setVisibility(View.VISIBLE);
                        emptyPostsState.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    /**
     * Sets up child-specific tabs for parent users.
     * Each tab shows posts for one child’s classroom.
     *
     * @param view fragment root view
     * @param feedViewModel shared feed ViewModel
     * @param parent current parent user
     */
    private void setupParentTabs(@NonNull View view,
                                 @NonNull FeedViewModel feedViewModel,
                                 @NonNull Parent parent) {

        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setVisibility(View.GONE);

        View emptyPostsState = view.findViewById(R.id.emptyPostsState);
        if (emptyPostsState != null) {
            emptyPostsState.setVisibility(View.GONE);
        }

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