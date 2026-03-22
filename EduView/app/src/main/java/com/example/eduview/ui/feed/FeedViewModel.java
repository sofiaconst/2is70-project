package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.FeedRepository;
import com.example.eduview.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedViewModel extends ViewModel {

    private FeedRepository feedRepository = new FeedRepository();
    private UserRepository userRepository = new UserRepository();

    private LiveData<List<FeedItem>> publishedPosts;
    private LiveData<List<FeedItem>> announcements;
    private LiveData<List<FeedItem>> pendingPosts;

    private final MutableLiveData<List<Student>> parentChildren = new MutableLiveData<>(new ArrayList<>());

    private String classroomId;

    public void loadPostsForUser(User user) {
        classroomId = null;
        if (user instanceof Student) {
            classroomId = ((Student) user).getClassId();

        } else if (user instanceof Teacher) {
            classroomId = ((Teacher) user).getClassId();
        }
        Log.d("FeedViewModel", "Class id found for class: " + classroomId);
    }

    public void loadChildrenForParent(Parent parent) {
        List<String> childIds = parent.getChildrenIDs();

        if (childIds == null || childIds.isEmpty()) {
            parentChildren.setValue(new ArrayList<>());
            return;
        }

        List<Student> children = new ArrayList<>();
        final int[] remaining = {childIds.size()};

        for (String childId : childIds) {
            userRepository.getUserById(childId, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user instanceof Student) {
                        Student student = (Student) user;

                        Log.d("FeedViewModel", "Parent child fetched: "
                                + student.getFirstName()
                                + " | userId=" + student.getUserId()
                                + " | classId=" + student.getClassId());

                        if (student.getClassId() != null && !student.getClassId().trim().isEmpty()) {
                            children.add(student);
                        } else {
                            Log.d("FeedViewModel", "Skipping child without classroom: " + student.getFirstName());
                        }
                    }

                    remaining[0]--;
                    if (remaining[0] == 0) {
                        parentChildren.setValue(children);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("FeedViewModel", "Failed to fetch child user", e);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        parentChildren.setValue(children);
                    }
                }
            });
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

    public LiveData<List<Student>> getParentChildren() {
        return parentChildren;
    }

    public void approvePost(String postId) {
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedViewModel", "Cannot approve post, classroomId is null");
            return;
        }
        feedRepository.approvePost(classroomId, postId);
        loadPendingPosts();
    }

    public void rejectPost(String postId) {
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedViewModel", "Cannot reject post, classroomId is null");
            return;
        }
        feedRepository.rejectPost(classroomId, postId);
        loadPendingPosts();
    }
}