package com.example.eduview.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

        recyclerPosts = view.findViewById(R.id.recyclerPosts);

        feedAdapter = new FeedAdapter();

        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPosts.setAdapter(feedAdapter);

        // Fake data for testing
        List<FeedItem> items = new ArrayList<>();

        FeedItem post1 = new FeedItem(FeedType.POST, "John Doe", "Check out my science project on plant cells!");
        FeedItem post2 = new FeedItem(FeedType.POST, "Emma Smith", "Today we learned about volcanoes in class.");
        FeedItem post3 = new FeedItem(FeedType.POST, "Liam Brown", "I finished my math worksheet!");

        items.add(post1);
        items.add(post2);
        items.add(post3);

        feedAdapter.setItems(items);
    }
}