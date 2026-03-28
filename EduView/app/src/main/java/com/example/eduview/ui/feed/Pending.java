package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;

/**
 * Fragment for displaying pending posts.
 * Shows a list of pending posts or an empty state if no items exist.
 */
public class Pending extends Fragment {

    // For listing items
    private RecyclerView recyclerPending;
    private FeedAdapter feedAdapter;

    public Pending() {
        // Required empty public constructor
    }

    /**
     * Inflates the pending fragment layout.
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
        return inflater.inflate(R.layout.fragment_pending, container, false);
    }

    /**
     * Initializes RecyclerView, adapter, and observers after the view is created.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get UI references from xml files
        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPending);
        View emptyPendingState = view.findViewById(R.id.emptyPendingState);

        // Get the FeedViewModel from the parent fragment
        FeedViewModel feedViewModel = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);

        // Initialize adapter with ViewModel
        feedAdapter = new FeedAdapter(feedViewModel);

        // Set up RecyclerView
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(feedAdapter);

        feedViewModel.loadPendingPosts();

        // Observe Pending Post data
        observePending(feedViewModel, recyclerPosts, emptyPendingState);
        feedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            observePending(feedViewModel, recyclerPosts, emptyPendingState);
        });
    }

    /**
     * Observes pending posts and updates UI accordingly.
     *
     * @param feedViewModel ViewModel containing pending posts
     * @param recyclerPosts RecyclerView displaying posts
     * @param emptyPendingState View shown when there are no posts
     */
    private void observePending(@NonNull FeedViewModel feedViewModel,
                                @NonNull RecyclerView recyclerPosts,
                                @NonNull View emptyPendingState) {
        // Ensure LiveData exists before fetching
        if (feedViewModel.getPendingPosts() != null) {
            feedViewModel.getPendingPosts().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    // Update the adapter with new data
                    feedAdapter.setItems(items);

                    if (items.isEmpty()) {
                        recyclerPosts.setVisibility(View.GONE);
                        emptyPendingState.setVisibility(View.VISIBLE);
                    } else {
                        recyclerPosts.setVisibility(View.VISIBLE);
                        emptyPendingState.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}