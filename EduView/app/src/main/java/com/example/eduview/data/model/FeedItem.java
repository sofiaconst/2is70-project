package com.example.eduview.data.model;

import android.view.ViewDebug;

import com.google.firebase.database.Exclude;

public class FeedItem {
    @Exclude
    private FeedItemType type;
    @Exclude
    private String postId;
    @Exclude
    private String authorName;

    private String authorId;
    private String caption;
    private String imageUrl;
    private long timestamp;

    @Exclude
    private String authorPfpName;

    public FeedItem(FeedItemType type, String authorId, String caption) {
        this.type = type;
        this.authorId = authorId;
        this.caption = caption;
        authorName = "";
    }
    @Exclude
    public FeedItemType getType() {
        return type;
    }
    @Exclude
    public String getPostId() {
        return postId;
    }
    @Exclude
    public String getAuthorName() {
        return authorName;
    }
    public String getAuthorId() { return authorId; }

    public String getCaption() {
        return caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() { return timestamp; }
    @Exclude
    public String getAuthorPfpName() {
        return authorPfpName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAuthorPfpName(String authorPfpName) {
        this.authorPfpName = authorPfpName;
    }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}