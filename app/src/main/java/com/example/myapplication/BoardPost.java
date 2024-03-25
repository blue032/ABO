package com.example.myapplication;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BoardPost {
    private String title;
    private String content;
    private long timestamp;
    private String userName;
    private String postId;

    // Constructor with title, content, and timestamp
    public BoardPost(String title, String content, long timestamp, String userName) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp; // Set the timestamp
        this.userName = userName;

    }

    // Firebase가 기본 생성자를 요구하므로, 매개변수 없는 생성자도 추가
    public BoardPost() {
        // 기본 생성자
    }

    public String getTitle() {
        return this.title;
    }

    public long getTimestamp() {
        // Make sure this method returns a long value
        return this.timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 날짜를 포매팅하는 메서드 추가
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    // userName의 getter와 setter 추가
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPostId(String postId){
        this.postId = postId;
    }
    public String getPostId(){
        return postId;
    }
}