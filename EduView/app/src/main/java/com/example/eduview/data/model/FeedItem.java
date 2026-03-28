package com.example.eduview.data.model;

import android.view.ViewDebug;

import com.google.firebase.database.Exclude;

/**
 * Represents a feed item (published post, announcement, pending post in the system.
 * Contains information about the type, ID and contents of the feed item.
 */
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
    @Exclude
    private boolean authorIsTeacher;

    /**
     * Creates a new FeedItem with a type, author ID, caption and sets the author name to empty.
     * @param type type of FeedItem
     * @param authorId the feed item author's ID
     * @param caption the caption of the feed item
     */
    public FeedItem(FeedItemType type, String authorId, String caption) {
        this.type = type;
        this.authorId = authorId;
        this.caption = caption;
        authorName = "";
    }

    /**
     * Returns the type of the feed item.
     * @return the type of the feed item
     */
    @Exclude
    public FeedItemType getType() {
        return type;
    }

    /**
     * Returns the ID of the feed item.
     * @return the ID of the feed item
     */
    @Exclude
    public String getPostId() {
        return postId;
    }

    /**
     * Returns the author of the feed item
     * @return the author of the feed item
     */
    @Exclude
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Returns the ID of the feed item author.
     * @return the ID of the feed item author
     */
    public String getAuthorId() { return authorId; }

    /**
     * Returns the caption of the feed item.
     * @return the caption of the feed item
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the image URL.
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Returns the time in which the feed item was created.
     * @return the time in which the feed item was created
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Returns the name of the picture that the author uses in their profile.
     * @return the name of the picture that the author uses in their profile
     */
    @Exclude
    public String getAuthorPfpName() {
        return authorPfpName;
    }

    /**
     * Returns if the feed item author is a teacher.
     * @return whether the feed item author is a teacher
     */
    @Exclude
    public boolean isTeacher() {
        return authorIsTeacher;
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
    public void setAuthorIsTeacher(boolean isTeacher) {
        this.authorIsTeacher = isTeacher;
    }
}