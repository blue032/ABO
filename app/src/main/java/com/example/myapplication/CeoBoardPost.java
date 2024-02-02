package com.example.myapplication;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CeoBoardPost {
    private String title;
    private String content;
    private long timestamp;

    // Constructor with title, content, and timestamp
    public CeoBoardPost(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp; // Set the timestamp
    }

    // Firebase가 기본 생성자를 요구하므로, 매개변수 없는 생성자도 추가
    public CeoBoardPost() {
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
}