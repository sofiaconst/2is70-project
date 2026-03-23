package com.example.eduview.data.model;

import static com.example.eduview.data.model.UserRole.STUDENT;

import android.util.Log;

import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.database.Exclude;

public class Post {
    private String authorId;
    private String caption;
    private String imageUrl;
    private long timestamp;

//    @Exclude
//    private User author;
    @Exclude
    private PostType postType;

    public Post() {}

    public Post(String authorId, String caption, String imageUrl) {
        this.authorId = authorId;
        this.caption = caption;
        this.imageUrl = imageUrl;
    }

    public PostType getPostType() {
        return postType;
    }

    public String getCaption() {
        return caption;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

//    public User getAuthor() {
//        return author;
//    }
//
//    public void loadAuthor() {
//        UserRepository repo = new UserRepository();
//
//        repo.fetchUser(authorId,
//                user -> this.author = user,
//                error -> Log.e("Post", "Failed to load author", error)
//        );
//    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

//    public void setAuthor(User author) {
//        this.author = author;
//    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
