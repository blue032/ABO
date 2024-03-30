package com.example.myapplication;

public class Notification {
    private String commentContent;
    private String postId;
    private String commentId;
    private String postOwnerId;
    private String commenterId;
    private long timestamp;
    private boolean isRead;
    private String postUserName; // 게시물 작성자의 사용자 이름

    // 기본 생성자
    public Notification() {}

    // 모든 필드를 포함하는 생성자
    public Notification(String commentContent, String postId, String commentId, String postOwnerId, String commenterId, long timestamp, boolean isRead, String postUserName) {
        this.commentContent = commentContent;
        this.postId = postId;
        this.commentId = commentId;
        this.postOwnerId = postOwnerId;
        this.commenterId = commenterId;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.postUserName = postUserName;
    }

    // Getter 및 Setter 메서드들
    public String getCommentContent() { return commentContent; }
    public void setCommentContent(String commentContent) { this.commentContent = commentContent; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostOwnerId() { return postOwnerId; }
    public void setPostOwnerId(String postOwnerId) { this.postOwnerId = postOwnerId; }

    public String getCommenterId() { return commenterId; }
    public void setCommenterId(String commenterId) { this.commenterId = commenterId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getPostUserName() { return postUserName; } // 추가된 getter 메서드
    public void setPostUserName(String postUserName) { this.postUserName = postUserName; } // 추가된 setter 메서드
}