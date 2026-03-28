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
 * Fragment responsible for displaying announcements.
 * Shows a standard announcements list for teachers and students,
 * and child-specific tabs for parents.
 */
public class Announcements extends Fragment {

    private RecyclerView recyclerAnnouncement;
    private FeedAdapter feedAdapter;

    /**
     * Default constructor.
     */
    public Announcements() {
        // Required empty public constructor
    }

    /**
     * Inflates the announcements fragment layout.
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
        return inflater.inflate(R.layout.fragment_announcements, container, false);
    }

    /**
     * Initializes the appropriate UI depending on the user's role.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        FeedViewModel feedViewModel = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);

        User currentUser = mainViewModel.getCurrentUser();
        if (currentUser == null) {
            Log.e("AnnouncementsFragment", "Current user is null");
            return;
        }

        if (currentUser instanceof Parent) {
            setupParentTabs(view, feedViewModel, (Parent) currentUser);
        } else {
            setupNormalAnnouncementsList(view, feedViewModel);
        }
    }

    /**
     * Sets up the announcements list for students and teachers.
     *
     * @param view fragment root view
     * @param feedViewModel shared feed ViewModel
     */
    private void setupNormalAnnouncementsList(@NonNull View view, @NonNull FeedViewModel feedViewModel) {
        RecyclerView recyclerAnnouncements = view.findViewById(R.id.recyclerAnnouncements);
        View emptyAnnouncementsState = view.findViewById(R.id.emptyAnnouncementsState);

        recyclerAnnouncements.setVisibility(View.VISIBLE);

        feedAdapter = new FeedAdapter(feedViewModel);
        recyclerAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAnnouncements.setAdapter(feedAdapter);

        View parentTabsContainer = view.findViewById(R.id.parentAnnouncementTabsContainer);
        if (parentTabsContainer != null) {
            parentTabsContainer.setVisibility(View.GONE);
        }

        feedViewModel.loadAnnouncements();

        observeAnnouncements(feedViewModel, recyclerAnnouncements, emptyAnnouncementsState);
        feedViewModel.getRefreshTrigger().observe(getViewLifecycleOwner(), refreshCount -> {
            observeAnnouncements(feedViewModel, recyclerAnnouncements, emptyAnnouncementsState);
        });
    }

    /**
     * Observes announcements and updates the list or empty state.
     *
     * @param feedViewModel shared feed ViewModel
     * @param recyclerAnnouncements RecyclerView displaying announcements
     * @param emptyAnnouncementsState view shown when there are no announcements
     */
    private void observeAnnouncements(@NonNull FeedViewModel feedViewModel,
                                      @NonNull RecyclerView recyclerAnnouncements,
                                      @NonNull View emptyAnnouncementsState) {
        if (feedViewModel.getAnnouncements() != null) {
            feedViewModel.getAnnouncements().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    feedAdapter.setItems(items);

                    if (items.isEmpty()) {
                        recyclerAnnouncements.setVisibility(View.GONE);
                        emptyAnnouncementsState.setVisibility(View.VISIBLE);
                    } else {
                        recyclerAnnouncements.setVisibility(View.VISIBLE);
                        emptyAnnouncementsState.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    /**
     * Sets up child-specific announcement tabs for parent users.
     * Each tab shows announcements for one child’s classroom.
     *
     * @param view fragment root view
     * @param feedViewModel shared feed ViewModel
     * @param parent current parent user
     */
    private void setupParentTabs(@NonNull View view,
                                 @NonNull FeedViewModel feedViewModel,
                                 @NonNull Parent parent) {

        RecyclerView recyclerAnnouncements = view.findViewById(R.id.recyclerAnnouncements);
        recyclerAnnouncements.setVisibility(View.GONE);

        View emptyAnnouncementsState = view.findViewById(R.id.emptyAnnouncementsState);
        if (emptyAnnouncementsState != null) {
            emptyAnnouncementsState.setVisibility(View.GONE);
        }

        View parentTabsContainer = view.findViewById(R.id.parentAnnouncementTabsContainer);
        parentTabsContainer.setVisibility(View.VISIBLE);

        TabLayout childTabs = view.findViewById(R.id.childAnnouncementTabs);
        ViewPager2 childViewPager = view.findViewById(R.id.childAnnouncementViewPager);

        feedViewModel.loadChildrenForParent(parent);

        feedViewModel.getParentChildren().observe(getViewLifecycleOwner(), children -> {
            if (children == null || children.isEmpty()) {
                Log.d("AnnouncementsFragment", "No parent children with valid classrooms");
                childTabs.setVisibility(View.GONE);
                childViewPager.setVisibility(View.GONE);
                return;
            }

            childTabs.setVisibility(View.VISIBLE);
            childViewPager.setVisibility(View.VISIBLE);

            ParentAnnouncementsPagerAdapter adapter = new ParentAnnouncementsPagerAdapter(this, children);
            childViewPager.setAdapter(adapter);

            new TabLayoutMediator(childTabs, childViewPager, (tab, position) -> {
                Student child = children.get(position);
                tab.setText(child.getFirstName());
            }).attach();
        });
    }
}