package com.example.eduview.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.FeedItemType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository for loading and moderating classroom feed data from Firebase Database.
 */
public class FeedRepository {

    private final DatabaseReference classroomRef;
    private final DatabaseReference postsRef;
    private final DatabaseReference userRef;

    /**
     * Creates a repository with default Firebase references.
     */
    public FeedRepository() {
        this(
                FirebaseDatabase.getInstance().getReference("classrooms"),
                FirebaseDatabase.getInstance().getReference("posts"),
                FirebaseDatabase.getInstance().getReference("users")
        );
    }

    /**
     * Creates a repository with in use Firebase references.
     *
     * @param classroomRef reference to classrooms data
     * @param postsRef reference to posts data
     * @param userRef reference to users data
     */
    public FeedRepository(DatabaseReference classroomRef,
                          DatabaseReference postsRef,
                          DatabaseReference userRef) {
        this.classroomRef = classroomRef;
        this.postsRef = postsRef;
        this.userRef = userRef;
    }

    /**
     * Loads published posts for a classroom.
     *
     * @param classroomId classroom identifier
     * @return LiveData containing published posts
     */
    public LiveData<List<FeedItem>> fetchPublishedPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

        // LiveData is returned immediately and will be filled later.
        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        // Guard against invalid classroom IDs to avoid errors.
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchPublishedPosts: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        // Navigate to the published posts list stored under the classroom feed node.
        classroomRef.child(classroomId)
                .child("feed")
                .child("published_posts")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("FeedRepository", "Published posts snapshot received");

                    // If no posts exist yet, return an empty list.
                    if (!snapshot.exists()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();

                    // Collect the post IDs.
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    // Reverse the IDs so newer inserted items are processed earlier.
                    Collections.reverse(postIds);

                    // Resolve the IDs into actual FeedItem objects.
                    fetchFeedItem(postIds, FeedItemType.PUBLISHED, liveData);
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);

                    // On failure, return an empty list.
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /**
     * Loads pending posts for a classroom.
     *
     * @param classroomId classroom identifier
     * @return LiveData containing pending posts
     */
    public LiveData<List<FeedItem>> fetchPendingPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching pending posts for classroom: " + classroomId);

        // This LiveData will eventually contain all pending feed items.
        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        // Stop early if the classroom ID is missing.
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchPendingPosts: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        // Request the pending post references for the classroom.
        classroomRef.child(classroomId)
                .child("feed")
                .child("pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("FeedRepository", "Pending posts snapshot received");

                    // No pending node means there are simply no pending posts.
                    if (!snapshot.exists()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();

                    // Extract all pending post IDs from the snapshot.
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    // Reverse order so newer references appear first.
                    Collections.reverse(postIds);

                    // Resolve each ID into a feed item.
                    fetchFeedItem(postIds, FeedItemType.PENDING, liveData);
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);

                    // Publish an empty result if the fetch fails.
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /**
     * Loads announcements for a classroom.
     *
     * @param classroomId classroom identifier
     * @return LiveData containing announcements
     */
    public LiveData<List<FeedItem>> fetchAnnouncements(String classroomId) {
        Log.d("FeedRepository", "Fetching announcements for classroom: " + classroomId);

        // This observable list will be updated once Firebase returns the data.
        MutableLiveData<List<FeedItem>> liveData = new MutableLiveData<>();

        // Validate input before reading from the database.
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedRepository", "fetchAnnouncements: classroomId is null");
            liveData.setValue(new ArrayList<>());
            return liveData;
        }

        // Read the announcements references for the classroom feed.
        classroomRef.child(classroomId)
                .child("feed")
                .child("announcements")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("FeedRepository", "Announcements snapshot received");

                    // If the announcements node is missing, return an empty list.
                    if (!snapshot.exists()) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<String> postIds = new ArrayList<>();

                    // Each child key represents one announcement post ID.
                    for (DataSnapshot postRef : snapshot.getChildren()) {
                        String postId = postRef.getKey();
                        if (postId != null) {
                            postIds.add(postId);
                        }
                    }

                    // Reverse the order before detailed loading.
                    Collections.reverse(postIds);

                    // Load the full feed items for those announcement IDs.
                    fetchFeedItem(postIds, FeedItemType.ANNOUNCEMENT, liveData);
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedRepository", "Failed to fetch post ids", e);

                    // Still notify observers with an empty set.
                    liveData.setValue(new ArrayList<>());
                });

        return liveData;
    }

    /**
     * Resolves post IDs into full feed items and enriches them with author data when available.
     *
     * @param postIds post identifiers to resolve
     * @param feedItemType type assigned to each resulting item
     * @param liveData target LiveData for the final list
     */
    private void fetchFeedItem(List<String> postIds, FeedItemType feedItemType, MutableLiveData<List<FeedItem>> liveData) {
        ArrayList<FeedItem> items = new ArrayList<>();

        // If there are no IDs to resolve, publish an empty result immediately.
        if (postIds.isEmpty()) {
            liveData.setValue(items);
            return;
        }

        // Tracks how many full item-building operations have finished.
        final int[] completed = {0};

        for (String postId : postIds) {
            // Load the raw post data first.
            postsRef.child(postId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String author = snapshot.child("authorId").getValue(String.class);
                        String content = snapshot.child("caption").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                        // Normalize nullable values so FeedItem creation stays safe.
                        if (content == null) content = "";
                        if (timestamp == null) timestamp = 0L;

                        // If the author is missing, create the item without author details.
                        if (author == null) {
                            FeedItem item = new FeedItem(feedItemType, null, content);
                            item.setPostId(postId);
                            item.setImageUrl(imageUrl);
                            item.setTimestamp(timestamp);
                            item.setAuthorIsTeacher(false);
                            items.add(item);

                            // Count this item as finished because no more lookups are needed.
                            completed[0]++;

                            if (completed[0] == postIds.size()) {
                                publishSortedItems(items, liveData);
                            }
                            return;
                        }

                        String finalContent = content;
                        long finalTimestamp = timestamp;

                        // Load author profile data like display name and profile picture.
                        userRef.child(author)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    String firstName = userSnapshot.child("first_name").getValue(String.class);
                                    String lastName = userSnapshot.child("last_name").getValue(String.class);
                                    String pfp = userSnapshot.child("pfp").getValue(String.class);

                                    // Replace missing names with empty strings before combining them.
                                    if (firstName == null) firstName = "";
                                    if (lastName == null) lastName = "";
                                    String authorName = (firstName + " " + lastName).trim();

                                    // Build the item with all post and author information gathered so far.
                                    FeedItem item = new FeedItem(feedItemType, author, finalContent);
                                    item.setAuthorName(authorName);
                                    item.setPostId(postId);
                                    item.setImageUrl(imageUrl);
                                    item.setTimestamp(finalTimestamp);
                                    item.setAuthorPfpName(pfp);

                                    // Check teacher role last, then mark the item as completed.
                                    postAuthorIsTeacher(author, isTeacher -> {
                                        item.setAuthorIsTeacher(isTeacher);
                                        items.add(item);
                                        completed[0]++;

                                        // Only publish when every item has fully finished loading.
                                        if (completed[0] == postIds.size()) {
                                            publishSortedItems(items, liveData);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FeedRepository", "Failed to fetch author name", e);

                                    // If author details fail, still keep the post itself.
                                    FeedItem item = new FeedItem(feedItemType, author, finalContent);
                                    item.setPostId(postId);
                                    item.setImageUrl(imageUrl);
                                    item.setTimestamp(finalTimestamp);

                                    // Still resolve whether the author is a teacher before finishing.
                                    postAuthorIsTeacher(author, isTeacher -> {
                                        item.setAuthorIsTeacher(isTeacher);
                                        items.add(item);
                                        completed[0]++;

                                        if (completed[0] == postIds.size()) {
                                            publishSortedItems(items, liveData);
                                        }
                                    });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FeedRepository", "Failed to fetch post", e);

                        // Even failed posts must count toward completion so the batch can finish.
                        completed[0]++;

                        if (completed[0] == postIds.size()) {
                            publishSortedItems(items, liveData);
                        }
                    });
        }
    }

    /**
     * Sorts items by timestamp when available and publishes them.
     *
     * @param items feed items to publish
     * @param liveData target LiveData
     */
    private void publishSortedItems(List<FeedItem> items, MutableLiveData<List<FeedItem>> liveData) {
        boolean hasRealTimestamps = false;

        // Check whether sorting by timestamp is meaningful.
        for (FeedItem feedItem : items) {
            if (feedItem.getTimestamp() > 0) {
                hasRealTimestamps = true;
                break;
            }
        }

        // Only sort when at least one real timestamp exists.
        if (hasRealTimestamps) {
            items.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        }

        // Publish the final list to observers.
        liveData.setValue(items);
    }

    /**
     * Moves a pending post into the published list.
     *
     * @param classroomId classroom identifier
     * @param postId post identifier
     */
    public void approvePost(String classroomId, String postId) {
        // Reference to the pending entry that should be removed after approval.
        DatabaseReference pendingRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("pending")
                .child(postId);

        // Reference to the published entry where the approved post should be added.
        DatabaseReference publishedRef = classroomRef
                .child(classroomId)
                .child("feed")
                .child("published_posts")
                .child(postId);

        // First mark the post as published, then remove it from pending.
        publishedRef.setValue(true)
                .addOnSuccessListener(aVoid -> pendingRef.removeValue())
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to approve post", e)
                );
    }

    /**
     * Removes a pending post from the classroom feed.
     *
     * @param classroomId classroom identifier
     * @param postId post identifier
     */
    public void rejectPost(String classroomId, String postId) {
        // Reference to the pending post entry inside the classroom feed.
        DatabaseReference pendingRef = classroomRef.child(classroomId)
                .child("feed")
                .child("pending")
                .child(postId);

        // Reference to the actual post object stored in the posts node.
        DatabaseReference postRef = postsRef.child(postId);

        // First remove the pending reference from the classroom.
        pendingRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // After removing it from pending, also delete the real post data.
                    postRef.removeValue()
                            .addOnFailureListener(e ->
                                    Log.e("FeedRepository", "Failed to delete post from posts node", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to reject post", e)
                );
    }

    /**
     * Checks whether a given post author is a teacher.
     *
     * @param authorId the ID of the user to check
     * @param callback returns true if the user is a teacher, false otherwise
     */
    public void postAuthorIsTeacher(String authorId, java.util.function.Consumer<Boolean> callback) {

        // Fetch the user node corresponding to the author ID.
        userRef.child(authorId).get().addOnSuccessListener(snapshot -> {

            // Read the role field from the user object.
            String role = snapshot.child("role").getValue(String.class);

            // Check if the role exists and matches Teacher.
            if (role != null && role.equalsIgnoreCase("Teacher")) {
                callback.accept(true);
            } else {
                callback.accept(false);
            }

        }).addOnFailureListener(e -> {
            Log.e("FeedRepository", "Failed to check author role", e);

            // On failure, default to false to keep UI stable.
            callback.accept(false);
        });
    }
}