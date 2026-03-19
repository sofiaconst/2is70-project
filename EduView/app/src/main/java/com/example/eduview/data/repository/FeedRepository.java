package com.example.eduview.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.FeedType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FeedRepository {

    private DatabaseReference classroomRef = FirebaseDatabase.getInstance().getReference("classrooms");
    private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    private DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

    public LiveData<List<FeedItem>> fetchPublishedPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        classroomRef.child(classroomId)
                .child("feed")
                .child("published_posts")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Published posts snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No published posts found for classroom: " + classroomId);
                        return;
                    }

                    List<String> postIds = new ArrayList<String>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }
                    fetchPosts(postIds, FeedType.POST, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }

    public LiveData<List<FeedItem>> fetchPendingPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        classroomRef.child(classroomId)
                .child("feed")
                .child("pending")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Published posts snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No published posts found for classroom: " + classroomId);
                        return;
                    }

                    List<String> postIds = new ArrayList<String>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }
                    fetchPosts(postIds, FeedType.PENDING, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }

    public LiveData<List<FeedItem>> fetchAnnouncements(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        classroomRef.child(classroomId)
                .child("feed")
                .child("announcements")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Published posts snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No published posts found for classroom: " + classroomId);
                        return;
                    }

                    List<String> postIds = new ArrayList<String>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }
                    fetchPosts(postIds, FeedType.ANNOUNCEMENT, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }
    private void fetchPosts(List<String> postIds, FeedType feedItemType, MutableLiveData<List<FeedItem>> liveData) {
        ArrayList<FeedItem> items = new ArrayList<>();

        if (postIds.isEmpty()) {
            liveData.setValue(items);
            return;
        }

        Log.d("FeedRepository", "Fetching Posts");

        final int[] completed = {0};

        for (String postId : postIds) {
            postsRef.child(postId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String author = snapshot.child("authorId").getValue(String.class);
                        String content = snapshot.child("caption").getValue(String.class);
                        String imageUrl = snapshot.child("image").getValue(String.class);

                        Log.d("FeedRepository", "Post content retrieved.");
                        if (content == null) content = "";

                        if (author == null) {
                            completed[0]++;
                            FeedItem item = new FeedItem(feedItemType, "", content);
                            item.setImageUrl(imageUrl);
                            items.add(item);

                            if (completed[0] == postIds.size()) {
                                liveData.setValue(items);
                            }

                            return;
                        }

                        String finalContent = content;
                        userRef.child(author)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    completed[0]++;

                                    String firstName = userSnapshot.child("first_name").getValue(String.class);
                                    String lastName = userSnapshot.child("last_name").getValue(String.class);

                                    if (firstName == null) firstName = "";
                                    if (lastName == null) lastName = "";
                                    String authorName = (firstName + " " + lastName).trim();

                                    FeedItem item = new FeedItem(feedItemType, authorName, finalContent);
                                    item.setImageUrl(imageUrl);
                                    items.add(item);

                                    if (completed[0] == postIds.size()) {
                                        liveData.setValue(items);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    Log.e("FeedRepository", "Failed to fetch author name", e);

                                    FeedItem item = new FeedItem(feedItemType, "", finalContent);
                                    item.setImageUrl(imageUrl);
                                    items.add(item);

                                    if (completed[0] == postIds.size()) {
                                        liveData.setValue(items);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        Log.e("FeedRepository", "Failed to fetch post", e);

                        if (completed[0] == postIds.size()) {
                            liveData.setValue(items);
                        }
                    });
        }
    }

    public void approvePost(String classroomId, String postId) {

        DatabaseReference pendingRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("pending_posts")
                .child(postId);

        DatabaseReference publishedRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("published_posts")
                .child(postId);

        // 1. Add to published
        publishedRef.setValue(true).addOnSuccessListener(aVoid -> {

            // 2. Remove from pending
            pendingRef.removeValue();

        }).addOnFailureListener(e ->
                Log.e("FeedRepository", "Failed to approve post", e)
        );
    }
    public void rejectPost(String classroomId, String postId) {

        classroomRef.child(classroomId)
                .child("feed")
                .child("pending_posts")
                .child(postId)
                .removeValue()
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to reject post", e)
                );
    }
}