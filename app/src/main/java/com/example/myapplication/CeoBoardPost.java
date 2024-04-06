package com.example.myapplication;

import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CeoBoardPost {
    private String title;
    private String content;
    private long timestamp;
    private String postId;
    private List<String> photoUrls;
    private String userName; // 사용자 이름을 저장할 새 필드

    // Constructor with title, content, timestamp, photoUrl, and userName
    public CeoBoardPost(String title, String content, long timestamp, List<String> photoUrls, String userName) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.photoUrls = photoUrls;
        this.userName = userName; // 사용자 이름 초기화
    }

    // userName 매개변수 없이 호출할 수 있는 새로운 생성자 추가
    public CeoBoardPost(String title, String content, long timestamp, List<String> photoUrls) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.photoUrls = photoUrls;
        this.userName = ""; // userName을 빈 문자열로 초기화하거나 다른 기본값 설정
    }

    // Firebase가 기본 생성자를 요구하므로, 매개변수 없는 생성자도 추가
    public CeoBoardPost() {
        // 기본 생성자
    }

    public String getTitle() {
        return this.title;
    }

    public long getTimestamp() {
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

    public void setPostId(String postId){
        this.postId = postId;
    }

    public String getPostId(){
        return postId;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    // userName에 대한 getter 및 setter 추가
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}