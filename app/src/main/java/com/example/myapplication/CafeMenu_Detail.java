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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CafeMenu_Detail extends AppCompatActivity {
    private TextView tvTitle;
    private TextView tvContent;
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

        // String URL 리스트를 Uri 리스트로 변환
        /*ArrayList<Uri>photoUris = new ArrayList<>();
        if (photoUrlsString != null) {
            for (String photoUrl : photoUrlsString) {
                photoUris.add(Uri.parse(photoUrl));
            }
        }*/


        // UI 컴포넌트와 연결
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        ImageView iconMore = findViewById(R.id.iconMore);
        iconMore.setOnClickListener(this::showPopupMenu);

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
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editPost();
                return true;
            } else if (itemId == R.id.action_delete) {
                deletePost();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editPost() {
        Intent intentFromDetail = getIntent();
        String title = intentFromDetail.getStringExtra("title");
        String content = intentFromDetail.getStringExtra("content");
        String postId = intentFromDetail.getStringExtra("postID"); // 'postID' 키를 확인하세요.

        ArrayList<String> photoUrlsString = getIntent().getStringArrayListExtra("photoUrls");

        Intent intentToEdit = new Intent(CafeMenu_Detail.this, CafeMenu_WriteBoard.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId); // 올바르게 'postId'를 추가하는지 확인
        if (photoUrlsString != null) {
            intentToEdit.putStringArrayListExtra("photoUrls", photoUrlsString);
        }
        startActivity(intentToEdit);
    }


    private void deletePost() {
        if (postId != null) {
            databaseReference.child(postId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CafeMenu_Detail.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CafeMenu_Detail.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(CafeMenu_Detail.this, "오류: postId가 null입니다.", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        // 데이터베이스에서 최신 데이터를 불러오는 로직 추가
        if (postId != null) {
            loadDataFromDatabase(postId);
        }
    }

    private void loadDataFromDatabase(String postId) {
        databaseReference.child(postId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String title = task.getResult().child("title").getValue(String.class);
                String content = task.getResult().child("content").getValue(String.class);
                // 결과를 UI에 반영합니다.
                tvTitle.setText(title);
                tvContent.setText(content);

                // 이미지 URI 리스트를 새로 구성합니다.
                List<String> newImageUrls = new ArrayList<>();
                DataSnapshot imagesSnapshot = task.getResult().child("photoUrls");
                if (imagesSnapshot.exists()) {
                    for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        newImageUrls.add(imageUrl);
                    }
                }

                // 이미지 URI 리스트를 업데이트합니다. (클래스 필드를 사용하세요)
                this.photoUris.clear();
                for (String imageUrl : newImageUrls) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        this.photoUris.add(Uri.parse(imageUrl));
                    }
                }

                // Adapter에 변경 사항을 알립니다.
                ImageAdapter adapter = (ImageAdapter) imagesRecyclerView.getAdapter();
                if (adapter != null) {
                    adapter.setImageUris(this.photoUris);
                    adapter.notifyDataSetChanged();
                } else {
                    // 어댑터가 null인 경우 새로운 어댑터를 생성하고 설정합니다.
                    adapter = new ImageAdapter(CafeMenu_Detail.this, this.photoUris);
                    imagesRecyclerView.setAdapter(adapter);
                }
            }
        });
    }



}