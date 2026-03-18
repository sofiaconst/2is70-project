package com.example.eduview.data.model;

import com.example.eduview.data.model.FeedType;

public class FeedItem {

    private FeedType type;

    private String authorName;
    private String content;
    private String imageUrl;

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

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
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
}