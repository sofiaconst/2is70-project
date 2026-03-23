package com.example.eduview.data.model;

public class FeedItem {

    private FeedType type;

    private String postId;

    private String authorName;
    private String content;
    private String imageUrl;
    private long timestamp;
    private String authorPfpName;

    // optional for pending
    private boolean isPending;

    public FeedItem(FeedType type, String authorName, String content) {
        this.type = type;
        this.authorName = authorName;
        this.content = content;
    }

    public FeedType getType() {
        return type;
    }

    public String getPostId() {
        return postId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAuthorPfpName() {
        return authorPfpName;
    }

    public boolean isPending() {
        return isPending;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPending(boolean pending) {
        isPending = pending;
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
}