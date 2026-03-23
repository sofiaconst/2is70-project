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
import java.util.Collections;
import java.util.List;

public class FeedRepository {

    private final DatabaseReference classroomRef = FirebaseDatabase.getInstance().getReference("classrooms");
    private final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

    public LiveData<List<FeedItem>> fetchPublishedPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchPublishedPosts: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        classroomRef.child(classroomId)
                .child("feed")
                .child("published_posts")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Published posts snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No published posts found for classroom: " + classroomId);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    Collections.reverse(postIds);
                    fetchFeedItem(postIds, FeedType.POST, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }

    public LiveData<List<FeedItem>> fetchPendingPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching pending posts for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchPendingPosts: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        classroomRef.child(classroomId)
                .child("feed")
                .child("pending")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Pending posts snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No pending posts found for classroom: " + classroomId);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    Collections.reverse(postIds);
                    fetchFeedItem(postIds, FeedType.PENDING, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }

    public LiveData<List<FeedItem>> fetchAnnouncements(String classroomId) {
        Log.d("FeedRepository", "Fetching announcements for classroom: " + classroomId);

        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchAnnouncements: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        classroomRef.child(classroomId)
                .child("feed")
                .child("announcements")
                .get()
                .addOnSuccessListener(snapshot -> {

                    Log.d("FeedRepository", "Announcements snapshot received");

                    if (!snapshot.exists()) {
                        Log.d("FeedRepository", "No announcements found for classroom: " + classroomId);
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    Collections.reverse(postIds);
                    fetchFeedItem(postIds, FeedType.ANNOUNCEMENT, liveData);
                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);
                    liveData.setValue(new ArrayList<>());
                });
        return liveData;
    }

    private void fetchFeedItem(List<String> postIds, FeedType feedItemType, MutableLiveData<List<FeedItem>> liveData) {
        ArrayList<FeedItem> items = new ArrayList<>();

        if (postIds.isEmpty()) {
            liveData.setValue(items);
            return;
        }

        Log.d("FeedRepository", "Fetching posts");
        final int[] completed = {0};

        for (String postId : postIds) {
            postsRef.child(postId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String author = snapshot.child("authorId").getValue(String.class);
                        String content = snapshot.child("caption").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                        if (content == null) content = "";
                        if (timestamp == null) timestamp = 0L;

                        Log.d("FeedRepository", "ImageURL " + imageUrl);
                        Log.d("FeedRepository", "Post content retrieved.");

                        if (author == null) {
                            completed[0]++;

                            FeedItem item = new FeedItem(feedItemType, "", content);
                            item.setPostId(postId);
                            item.setImageUrl(imageUrl);
                            item.setTimestamp(timestamp);
                            items.add(item);

                            if (completed[0] == postIds.size()) {
                                boolean hasRealTimestamps = false;
                                for (FeedItem feedItem : items) {
                                    if (feedItem.getTimestamp() > 0) {
                                        hasRealTimestamps = true;
                                        break;
                                    }
                                }

                                if (hasRealTimestamps) {
                                    items.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                                }

                                liveData.setValue(items);
                            }
                            return;
                        }

                        String finalContent = content;
                        Long finalTimestamp = timestamp;

                        userRef.child(author)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    completed[0]++;

                                    String firstName = userSnapshot.child("first_name").getValue(String.class);
                                    String lastName = userSnapshot.child("last_name").getValue(String.class);
                                    String pfp = userSnapshot.child("pfp").getValue(String.class);

                                    if (firstName == null) firstName = "";
                                    if (lastName == null) lastName = "";
                                    String authorName = (firstName + " " + lastName).trim();

                                    FeedItem item = new FeedItem(feedItemType, authorName, finalContent);
                                    item.setPostId(postId);
                                    item.setImageUrl(imageUrl);
                                    item.setTimestamp(finalTimestamp);
                                    item.setAuthorPfpName(pfp);
                                    items.add(item);

                                    if (completed[0] == postIds.size()) {
                                        boolean hasRealTimestamps = false;
                                        for (FeedItem feedItem : items) {
                                            if (feedItem.getTimestamp() > 0) {
                                                hasRealTimestamps = true;
                                                break;
                                            }
                                        }

                                        if (hasRealTimestamps) {
                                            items.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                                        }

                                        liveData.setValue(items);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    Log.e("FeedRepository", "Failed to fetch author name", e);

                                    FeedItem item = new FeedItem(feedItemType, "", finalContent);
                                    item.setPostId(postId);
                                    item.setImageUrl(imageUrl);
                                    item.setTimestamp(finalTimestamp);
                                    items.add(item);

                                    if (completed[0] == postIds.size()) {
                                        boolean hasRealTimestamps = false;
                                        for (FeedItem feedItem : items) {
                                            if (feedItem.getTimestamp() > 0) {
                                                hasRealTimestamps = true;
                                                break;
                                            }
                                        }

                                        if (hasRealTimestamps) {
                                            items.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                                        }

                                        liveData.setValue(items);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        Log.e("FeedRepository", "Failed to fetch post", e);

                        if (completed[0] == postIds.size()) {
                            boolean hasRealTimestamps = false;
                            for (FeedItem feedItem : items) {
                                if (feedItem.getTimestamp() > 0) {
                                    hasRealTimestamps = true;
                                    break;
                                }
                            }

                            if (hasRealTimestamps) {
                                items.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                            }

                            liveData.setValue(items);
                        }
                    });
        }
    }

    public void approvePost(String classroomId, String postId) {

        DatabaseReference pendingRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("pending")
                .child(postId);

        DatabaseReference publishedRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("published_posts")
                .child(postId);

        publishedRef.setValue(true).addOnSuccessListener(aVoid -> pendingRef.removeValue())
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to approve post", e)
                );
    }

    public void rejectPost(String classroomId, String postId) {
        classroomRef.child(classroomId)
                .child("feed")
                .child("pending")
                .child(postId)
                .removeValue()
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to reject post", e)
                );
    }
}