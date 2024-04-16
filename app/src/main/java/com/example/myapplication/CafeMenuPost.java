package com.example.myapplication;

import java.util.List;

//게시물을 관리하는 데 필요한 데이터 구조를 정의하는 클래스
public class CafeMenuPost {
    private String title;
    private String content;
    private List<String> photoUrls;
    private String postId; // 게시글 ID
    private long timestamp; // 게시글 타임스탬프


    // userName 매개변수 없이 호출할 수 있는 새로운 생성자 추가
    public CafeMenuPost(String title, String content, List<String> photoUrls) {
        this.title = title;
        this.content = content;
        this.photoUrls = photoUrls;
    }

    // Firebase가 기본 생성자를 요구하므로, 매개변수 없는 생성자도 추가
    public CafeMenuPost() {
        // 기본 생성자
    }
    // 게시글 ID를 설정하는 메소드
    public void setPostId(String postId) {
        this.postId = postId;
    }

    // 게시글 ID를 가져오는 메소드
    public String getPostId() {
        return postId;
    }

    // 타임스탬프를 가져오는 메소드
    public long getTimestamp() {
        return timestamp;
    }

    // 타임스탬프를 설정하는 메소드
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

}