package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.repository.FeedRepository;

import java.util.List;

/**
 * Fragment that displays published posts for a specific student's classroom
 * when viewed from the parent feed.
 */
public class ParentStudentPostsFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";

    private FeedAdapter feedAdapter;
    private String classroomId;
    private RecyclerView recyclerPosts;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;

    /**
     * Default constructor.
     */
    public ParentStudentPostsFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new fragment instance for the given classroom ID.
     *
     * @param classroomId ID of the classroom whose posts should be displayed
     * @return a configured ParentStudentPostsFragment instance
     */
    public static ParentStudentPostsFragment newInstance(String classroomId) {
        ParentStudentPostsFragment fragment = new ParentStudentPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASSROOM_ID, classroomId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the layout for the posts-only fragment.
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
        return inflater.inflate(R.layout.fragment_posts_list_only, container, false);
    }

    /**
     * Initializes views, adapter, empty state, and starts loading posts.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        recyclerPosts = view.findViewById(R.id.recyclerPostsOnly);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up empty state view
        emptyState = view.findViewById(R.id.emptyPostsOnlyState);
        emptyTitle = view.findViewById(R.id.tvEmptyPostsMessage);
        emptySubtitle = view.findViewById(R.id.tvEmptyPostsOnlySubtitle);

        // Get FeedViewModel from parent fragment
        FeedViewModel sharedFeedViewModel =
                new ViewModelProvider(requireParentFragment().requireParentFragment())
                        .get(FeedViewModel.class);

        // Initialize adapter
        feedAdapter = new FeedAdapter(sharedFeedViewModel);
        recyclerPosts.setAdapter(feedAdapter);

        // Read classroom ID
        classroomId = null;
        if (getArguments() != null) {
            classroomId = getArguments().getString(ARG_CLASSROOM_ID);
        }

        // Show UI if the child is not assigned to a classroom
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("ParentStudentPosts", "classroomId is null");
            recyclerPosts.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
                emptyTitle.setText("No Classroom");
                emptySubtitle.setText("This child is not in a class yet.");
            }
            return;
        }

        // Initial load
        loadPosts();

        // Reload posts when refreshed
        sharedFeedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            loadPosts();
        });
    }

    /**
     * Loads published posts for the current classroom and updates the UI.
     */
    private void loadPosts() {
        Log.d("ParentStudentPosts", "Loading classroomId=" + classroomId);

        // Fetch published posts directly for the given classroom
        FeedRepository feedRepository = new FeedRepository();
        LiveData<List<FeedItem>> postsLiveData = feedRepository.fetchPublishedPosts(classroomId);

        postsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                feedAdapter.setItems(items);

                if (emptyState != null) {
                    if (items.isEmpty()) {
                        // Show empty state if there are no posts
                        recyclerPosts.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                        emptyTitle.setText("No posts yet");
                        emptySubtitle.setText("There is nothing to show here yet.");
                    } else {
                        // Show posts and hide empty state
                        recyclerPosts.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}