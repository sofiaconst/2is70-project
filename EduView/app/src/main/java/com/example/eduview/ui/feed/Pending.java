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

public class Pending extends Fragment {

    private RecyclerView recyclerPending;
    private FeedAdapter feedAdapter;

    public Pending() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerPending = view.findViewById(R.id.recyclerPending);

        feedAdapter = new FeedAdapter();

        recyclerPending.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPending.setAdapter(feedAdapter);

        // Fake data for testing
        List<FeedItem> items = new ArrayList<>();

        FeedItem pen1 = new FeedItem(FeedType.PENDING, "John Doe", "Check out my science project on plant cells!");
        FeedItem pen2 = new FeedItem(FeedType.PENDING, "Emma Smith", "Today we learned about volcanoes in class.");
        FeedItem pen3 = new FeedItem(FeedType.PENDING, "Liam Brown", "I finished my math worksheet!");

        items.add(pen1);
        items.add(pen2);
        items.add(pen3);

        feedAdapter.setItems(items);
    }
}