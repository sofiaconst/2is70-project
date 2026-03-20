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

import com.example.eduview.R;
import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.FeedType;

import java.util.ArrayList;
import java.util.List;

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

        RecyclerView recyclerPosts = view.findViewById(R.id.recyclerPosts);
        Log.d("PostsFragment", "Create Recycler View");

        FeedViewModel feedViewModel = new ViewModelProvider(requireParentFragment()).get(FeedViewModel.class);
        feedAdapter = new FeedAdapter(feedViewModel);

        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(feedAdapter);
        Log.d("PostsFragment", "Feed Adapter");

        if (feedViewModel.getPublishedPosts() != null) {
            feedViewModel.getPublishedPosts().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    feedAdapter.setItems(items);
                }
            });
        }
    }
}