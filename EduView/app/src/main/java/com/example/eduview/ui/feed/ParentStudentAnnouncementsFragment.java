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

public class ParentStudentAnnouncementsFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";

    private FeedAdapter feedAdapter;
    private String classroomId;
    private RecyclerView recyclerAnnouncements;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;

    public ParentStudentAnnouncementsFragment() {
        // Required empty public constructor
    }

    public static ParentStudentAnnouncementsFragment newInstance(String classroomId) {
        ParentStudentAnnouncementsFragment fragment = new ParentStudentAnnouncementsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASSROOM_ID, classroomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_announcements_list_only, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerAnnouncements = view.findViewById(R.id.recyclerAnnouncementsOnly);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));

        emptyState = view.findViewById(R.id.emptyAnnouncementsOnlyState);
        emptyTitle = view.findViewById(R.id.tvEmptyAnnouncementsMessage);
        emptySubtitle = view.findViewById(R.id.tvEmptyAnnouncementsOnlySubtitle);

        FeedViewModel sharedFeedViewModel =
                new ViewModelProvider(requireParentFragment().requireParentFragment())
                        .get(FeedViewModel.class);

        feedAdapter = new FeedAdapter(sharedFeedViewModel);
        recyclerAnnouncements.setAdapter(feedAdapter);

        classroomId = null;
        if (getArguments() != null) {
            classroomId = getArguments().getString(ARG_CLASSROOM_ID);
        }

        if (classroomId == null || classroomId.isEmpty()) {
            recyclerAnnouncements.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
                emptyTitle.setText("No Classroom");
                emptySubtitle.setText("This child is not in a class yet.");
            }
            return;
        }

        loadAnnouncements();

        sharedFeedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            loadAnnouncements();
        });
    }

    private void loadAnnouncements() {
        FeedRepository feedRepository = new FeedRepository();
        LiveData<List<FeedItem>> announcementsLiveData = feedRepository.fetchAnnouncements(classroomId);

        announcementsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                feedAdapter.setItems(items);

                if (emptyState != null) {
                    if (items.isEmpty()) {
                        recyclerAnnouncements.setVisibility(View.GONE);
                        emptyState.setVisibility(View.VISIBLE);
                        emptyTitle.setText("No announcements yet");
                        emptySubtitle.setText("There is nothing to show here yet.");
                    } else {
                        recyclerAnnouncements.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}