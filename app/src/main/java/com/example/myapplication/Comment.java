package com.example.myapplication;

public class Comment {
    private String content; // 댓글 내용
    private String userId; // 댓글 작성자의 사용자 ID
    private long timestamp; // 댓글이 작성된 시간
    private String postId; // 댓글이 달린 게시물의 ID
    private String postUserName; // 게시물 작성자의 UID

    // 기본 생성자
    public Comment() {
    }

    // 모든 필드를 포함한 생성자
    public Comment(String content, String userId, long timestamp, String postId, String postUserName) {
        this.content = content;
        this.userId = userId;
        this.timestamp = timestamp;
        this.postId = postId;
        this.postUserName = postUserName; //게시글 작성자의 UID
    }

    // Getter 및 Setter 메서드
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostUserName() {
        return postUserName;
    }

    public void setPostUserName(String postUserName) {
        this.postUserName = postUserName;
    }
}
