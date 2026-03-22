package com.example.eduview.data.repository;

import com.example.eduview.data.model.Post;
import com.example.eduview.data.model.PostType;
import com.google.firebase.database.DataSnapshot;
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
     * Fetches a post from Firebase Realtime Database using its post ID.
     *
     * @param postId the unique ID of the post to retrieve
     * @param onSuccess callback invoked with the fetched Post if retrieval succeeds
     * @param onError callback invoked if the post does not exist or the read fails
     */
    public void fetchPost(String postId, Consumer<Post> onSuccess, Consumer<Exception> onError) {
        postRef.child(postId).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                onError.accept(new RuntimeException("Failed to fetch post"));
                return;
            }

            DataSnapshot snapshot = task.getResult();

            if (!snapshot.exists()) {
                onError.accept(new RuntimeException("Post not found"));
                return;
            }

            String caption = snapshot.child("caption").getValue(String.class);
            String imageUrl = snapshot.child("imageUrl").getValue(String.class);
            String authorId = snapshot.child("authorId").getValue(String.class);
            Long timestamp = snapshot.child("timestamp").getValue(Long.class);

            Post post = new Post(authorId, caption, imageUrl);
            post.setTimestamp(timestamp != null ? timestamp : 0L);

            onSuccess.accept(post);
        });
    }

    /**
     * Creates a new post in Firebase Realtime Database and links it to the
     * appropriate classroom feed category.
     *
     * @param type the type of post, used to determine whether the post is stored
     *             under announcements, pending, or published_posts
     * @param classId the ID of the classroom in which the post should appear
     * @param post the post object to store
     * @param onSuccess callback invoked with the generated post ID if creation succeeds
     * @param onError callback invoked if the post type is invalid, the post ID
     *                cannot be generated, or the Firebase write fails
     */
    public void createPost(PostType type,
                           String classId,
                           Post post,
                           Consumer<String> onSuccess,
                           Consumer<Exception> onError) {

        String feedPath;

        switch (type) {
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

        post.setTimestamp(System.currentTimeMillis());

        Map<String, Object> updates = new HashMap<>();
        updates.put("/posts/" + postId, post);
        updates.put("/classrooms/" + classId + "/feed/" + feedPath + "/" + postId, true);

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> onSuccess.accept(postId))
                .addOnFailureListener(onError::accept);
    }
}