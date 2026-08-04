package com.example.b07taam2026;

import java.util.Map;

// data model for a comment with likes and replies
public class Comment {

    private String id;
    private String author;
    private String text;
    private long timestamp;
    private Map<String, Boolean> likes;
    private Map<String, Comment> replies;

    // constructors (empty one required by firebase)
    public Comment() {}

    public Comment(String id, String author, String text, long timestamp) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.timestamp = timestamp;
    }

    // standard getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }
    public Map<String, Comment> getReplies() { return replies; }
    public void setReplies(Map<String, Comment> replies) { this.replies = replies; }

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    // whether the current user liked comment
    public boolean isLikedBy(String uid) {
        return uid != null && likes != null && likes.containsKey(uid);
    }
}
