package com.example.eduview.ui.feed;

import android.os.Bundle;
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
 * Fragment that displays announcements for a specific student's classroom
 * when viewed from the parent feed.
 */
public class ParentStudentAnnouncementsFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";

    private FeedAdapter feedAdapter;
    private String classroomId;
    private RecyclerView recyclerAnnouncements;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;

    /**
     * Default constructor.
     */
    public ParentStudentAnnouncementsFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new fragment instance for the given classroom ID.
     *
     * @param classroomId ID of the classroom whose announcements should be displayed
     * @return a configured ParentStudentAnnouncementsFragment instance
     */
    public static ParentStudentAnnouncementsFragment newInstance(String classroomId) {
        ParentStudentAnnouncementsFragment fragment = new ParentStudentAnnouncementsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASSROOM_ID, classroomId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the layout for the announcements fragment.
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
        return inflater.inflate(R.layout.fragment_announcements_list_only, container, false);
    }

    /**
     * Initializes views, adapter, empty state, and starts loading announcements.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        recyclerAnnouncements = view.findViewById(R.id.recyclerAnnouncementsOnly);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up empty state view
        emptyState = view.findViewById(R.id.emptyAnnouncementsOnlyState);
        emptyTitle = view.findViewById(R.id.tvEmptyAnnouncementsMessage);
        emptySubtitle = view.findViewById(R.id.tvEmptyAnnouncementsOnlySubtitle);

        // Get FeedViewModel from parent fragment
        FeedViewModel sharedFeedViewModel =
                new ViewModelProvider(requireParentFragment().requireParentFragment())
                        .get(FeedViewModel.class);

        // Initialize adapter
        feedAdapter = new FeedAdapter(sharedFeedViewModel);
        recyclerAnnouncements.setAdapter(feedAdapter);

        // Read classroom ID
        classroomId = null;
        if (getArguments() != null) {
            classroomId = getArguments().getString(ARG_CLASSROOM_ID);
        }

        // Show UI if the child is not assigned to a classroom
        if (classroomId == null || classroomId.isEmpty()) {
            recyclerAnnouncements.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
                emptyTitle.setText("No Classroom");
                emptySubtitle.setText("This child is not in a class yet.");
            }
            return;
        }

        // Initial load
        loadAnnouncements();

        // Reload announcements when refreshed
        sharedFeedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            loadAnnouncements();
        });
    }

    /**
     * Loads announcements for the current classroom and updates the UI.
     */
    private void loadAnnouncements() {
        // Fetch announcements directly for the given classroom
        FeedRepository feedRepository = new FeedRepository();
        LiveData<List<FeedItem>> announcementsLiveData = feedRepository.fetchAnnouncements(classroomId);

        announcementsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                feedAdapter.setItems(items);

                if (emptyState != null) {
                    if (items.isEmpty()) {
                        // Show empty state if there are no announcements
                        recyclerAnnouncements.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                        emptyTitle.setText("No announcements yet");
                        emptySubtitle.setText("There is nothing to show here yet.");
                    } else {
                        // Show announcements and hide empty state
                        recyclerAnnouncements.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}