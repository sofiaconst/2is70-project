package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.FeedRepository;

public class FeedViewModel extends ViewModel {

    private FeedRepository feedRepository = new FeedRepository();

    public void loadPostsForUser(User user) {
        String classroomId = null;

        if (user instanceof Student) {
            classroomId = ((Student) user).getClassId();
            Log.d("FeedViewModel", "Loading posts for classroom: " + classroomId);
        } else if (user instanceof Teacher) {
            classroomId = ((Teacher) user).getClassId();
        }

        if (classroomId != null) {
            Log.d("FeedViewModel", "Loading posts for classroom: " + classroomId);
            loadPublishedPosts(classroomId);
        }
        else {
            Log.w("FeedViewModel", "User is not a Student or Teacher, feed not loaded yet");
        }
    }

        private void loadPublishedPosts(String classroomId) {
            Log.d("FeedViewModel", "Request to load posts for classroom: " + classroomId);

            feedRepository.fetchPublishedPosts(classroomId);
        }
}