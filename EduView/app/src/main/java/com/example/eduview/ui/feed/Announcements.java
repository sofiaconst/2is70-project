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

public class Announcements extends Fragment {

    private RecyclerView recyclerAnnouncement;
    private FeedAdapter feedAdapter;

    public Announcements() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_announcements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerAnnouncement = view.findViewById(R.id.recyclerAnnouncements);

        feedAdapter = new FeedAdapter();

        recyclerAnnouncement.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAnnouncement.setAdapter(feedAdapter);

        // Fake data for testing
        List<FeedItem> items = new ArrayList<>();

        FeedItem ann1 = new FeedItem(FeedType.ANNOUNCEMENT, "Emily Smith", "We got a trip");
        FeedItem ann2 = new FeedItem(FeedType.ANNOUNCEMENT, "Emily Smith", "Today we learned about volcanoes in class.");
        FeedItem ann3 = new FeedItem(FeedType.ANNOUNCEMENT, "Emily Smith", "No more trip actually!");

        items.add(ann1);
        items.add(ann2);
        items.add(ann3);

        feedAdapter.setItems(items);
    }
}