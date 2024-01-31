package com.example.myapplication;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BoardPost {
    private String title;
    private String content;
    private long timestamp;

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
