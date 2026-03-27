package com.example.eduview.data.repository;

import com.example.eduview.data.model.FeedItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PostRepository {
    //Database reference to the posts node
    private final DatabaseReference postRef;
    private final DatabaseReference classRef;
    private final DatabaseReference rootRef;

    /**
     * Default constructor used by the application.
     * Initializes Firebase database references for the post node.
     */
    public PostRepository() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        postRef = db.getReference("posts");
        classRef = db.getReference("classrooms");
        rootRef = db.getReference();
    }

    /**
     * Constructor used for testing the application.
     * Initializes Firebase database references for the post node.
     */
    public PostRepository(DatabaseReference postRef, DatabaseReference classRef,
                          DatabaseReference rootRef) {
        this.postRef = postRef;
        this.classRef = classRef;
        this.rootRef = rootRef;
    }

    /**
     * Creates a new post in Firebase Realtime Database and links it to the
     * appropriate classroom feed category.
     *
     * @param classId the ID of the classroom in which the post should appear
     * @param feedItem the post object to store
     * @param onSuccess callback invoked with the generated post ID if creation succeeds
     * @param onError callback invoked if the post type is invalid, the post ID
     *                cannot be generated, or the Firebase write fails
     */
    public void createPost(String classId,
                           FeedItem feedItem,
                           Consumer<String> onSuccess,
                           Consumer<Exception> onError) {

        String feedPath;

        switch (feedItem.getType()) {
            case ANNOUNCEMENT:
                feedPath = "announcements";
                break;
            case PENDING:
                feedPath = "pending";
                break;
            case PUBLISHED:
                feedPath = "published_posts";
                break;
            default:
                onError.accept(new IllegalArgumentException("Invalid post type"));
                return;
        }

        String postId = postRef.push().getKey();

        if (postId == null) {
            onError.accept(new RuntimeException("Could not generate post ID"));
            return;
        }

        feedItem.setTimestamp(System.currentTimeMillis());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/posts/" + postId, feedItem);
        updates.put("/classrooms/" + classId + "/feed/" + feedPath + "/" + postId, true);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> onSuccess.accept(postId))
                .addOnFailureListener(onError::accept);
    }
}