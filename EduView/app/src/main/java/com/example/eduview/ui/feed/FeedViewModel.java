package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.FeedRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private FeedRepository feedRepository = new FeedRepository();
    private LiveData<List<FeedItem>> publishedPosts;
    private LiveData<List<FeedItem>> announcements;
    private LiveData<List<FeedItem>> pendingPosts;
    private String classroomId;

    public void loadPostsForUser(User user) {
        classroomId = null;
        if (user instanceof Student) {
            classroomId = ((Student) user).getClassId();
            Log.d("FeedViewModel", "Loading posts for classroom: " + classroomId);
        } else if (user instanceof Teacher) {
            classroomId = ((Teacher) user).getClassId();
        }
    }

    public void loadPublishedPosts() {
        publishedPosts = feedRepository.fetchPublishedPosts(classroomId);
    }

    public void loadAnnouncements() {
        announcements = feedRepository.fetchAnnouncements(classroomId);
    }

    public void loadPendingPosts() {
        pendingPosts = feedRepository.fetchPendingPosts(classroomId);
    }

    public LiveData<List<FeedItem>> getPublishedPosts() {
        return publishedPosts;
    }

    public LiveData<List<FeedItem>> getAnnouncements() {
        return announcements;
    }

    public LiveData<List<FeedItem>> getPendingPosts() {
        return pendingPosts;
    }
}