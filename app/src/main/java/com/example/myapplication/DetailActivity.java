package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 인텐트에서 데이터 가져오기
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        // timestamp 처리는 상황에 따라 다를 수 있습니다.

        // 가져온 데이터로 뷰를 설정
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        tvTitle.setText(title);
        tvContent.setText(content);
    }
}

