package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eduview.data.repository.FeedRepository;

public class FeedViewModel extends ViewModel {

    private FeedRepository feedRepository = new FeedRepository();

    public void loadPublishedPosts(String classroomId) {
        Log.d("FeedViewModel", "Request to load posts for classroom: " + classroomId);

        feedRepository.fetchPublishedPosts(classroomId);
    }
}