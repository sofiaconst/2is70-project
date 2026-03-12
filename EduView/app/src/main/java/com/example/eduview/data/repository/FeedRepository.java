package com.example.eduview.data.repository;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FeedRepository {

    private DatabaseReference classroomRef = FirebaseDatabase.getInstance().getReference("classrooms");
    private DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

    public void fetchPublishedPosts(String classroomId) {
        Log.d("FeedRepository", "Fetching published posts for classroom: " + classroomId);

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



                    for (DataSnapshot postRef : snapshot.getChildren()) {

                        String postId = postRef.getKey();
                        Log.d("FeedRepository", "Post ID found: " + postId);
                        if (postId == null) continue;

                        fetchPost(postId);
                    }

                    Log.d("FeedRepository", "Total posts found: " + snapshot.getChildrenCount());

                })
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to fetch post ids", e)
                );
    }


    private void fetchPost(String postId) {

        postsRef.child(postId)
                .get()
                .addOnSuccessListener(snapshot ->
                        Log.d("FeedRepository", "Post: " + snapshot.getValue())
                )
                .addOnFailureListener(e ->
                        Log.e("FeedRepository", "Failed to fetch post", e)
                );
    }
}