package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class ParentStudentPostsFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";

    private FeedAdapter feedAdapter;

    public ParentStudentPostsFragment() {
        // Required empty public constructor
    }

    public static ParentStudentPostsFragment newInstance(String classroomId) {
        ParentStudentPostsFragment fragment = new ParentStudentPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLASSROOM_ID, classroomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts_list_only, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPostsOnly);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        FeedViewModel sharedFeedViewModel =
                new ViewModelProvider(requireParentFragment().requireParentFragment())
                        .get(FeedViewModel.class);

        feedAdapter = new FeedAdapter(sharedFeedViewModel);
        recyclerPosts.setAdapter(feedAdapter);

        String classroomId = null;
        if (getArguments() != null) {
            classroomId = getArguments().getString(ARG_CLASSROOM_ID);
        }

        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("ParentStudentPosts", "classroomId is null");
            return;
        }

        Log.d("ParentStudentPosts", "Loading classroomId=" + classroomId);

        FeedRepository feedRepository = new FeedRepository();
        LiveData<List<FeedItem>> postsLiveData = feedRepository.fetchPublishedPosts(classroomId);

        postsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                feedAdapter.setItems(items);
            }
        });
    }
}