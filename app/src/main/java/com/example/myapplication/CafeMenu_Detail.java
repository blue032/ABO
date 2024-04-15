package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CafeMenu_Detail extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private String postId;

    private RecyclerView imagesRecyclerView;
    private ArrayList<Uri> photoUris; // 사진 URI를 저장할 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafemenu_detail);

        postId = getIntent().getStringExtra("postID");
        databaseReference = FirebaseDatabase.getInstance().getReference("OOcafemenu");

        // 인텐트에서 데이터 추출
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        ArrayList<String> photoUrlsString = intent.getStringArrayListExtra("photoUrls");

        // UI 컴포넌트와 연결
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);

        // 제목과 내용을 TextView에 설정
        tvTitle.setText(title);
        tvContent.setText(content);

        // RecyclerView를 위한 설정
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // URI 리스트 생성
        photoUris = new ArrayList<>();
        if (photoUrlsString != null) {
            for (String urlString : photoUrlsString) {
                photoUris.add(Uri.parse(urlString));
            }
        }

        // Adapter 생성 및 설정
        ImageAdapter adapter = new ImageAdapter(this, photoUris);
        imagesRecyclerView.setAdapter(adapter);



    }
}
