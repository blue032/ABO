package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.PopupMenu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

//글 등록 후, 글 세부 내용을 볼 수 있는 곳
public class CeoDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private String postId;
    private RecyclerView imagesRecyclerView;
    private ArrayList<String> photoUrls; // 사진 URL을 저장할 멤버 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_detail);

        postId = getIntent().getStringExtra("postId");
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        TextView tvNickname = findViewById(R.id.tvnickname);
        TextView tvTimestamp = findViewById(R.id.tvTimestamp);
        ImageView iconMore = findViewById(R.id.iconMore);

        // Fetching post data from Firebase
        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String content = dataSnapshot.child("content").getValue(String.class);
                    String userName = dataSnapshot.child("userName").getValue(String.class);
                    Long timestamp = dataSnapshot.child("timestamp").getValue(Long.class);

                    tvTitle.setText(title);
                    tvContent.setText(content);
                    tvTimestamp.setText(formatTimestampToKST(timestamp));

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("CeoUsers").child(userName);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String nickname = userSnapshot.child("Nickname").getValue(String.class);
                                tvNickname.setText(nickname);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(CeoDetailActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ArrayList<Uri> photoUris = new ArrayList<>();
                    for (DataSnapshot photoSnapshot : dataSnapshot.child("photoUrls").getChildren()) {
                        String photoUrl = photoSnapshot.getValue(String.class);
                        photoUris.add(Uri.parse(photoUrl));
                    }

                    imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
                    imagesRecyclerView.setLayoutManager(new LinearLayoutManager(CeoDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    ImageAdapter adapter = new ImageAdapter(CeoDetailActivity.this, photoUris);
                    imagesRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CeoDetailActivity.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
            }
        });

        iconMore.setOnClickListener(this::showPopupMenu);
    }
    private String formatTimestampToKST(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 한국 시간대로 설정
        return sdf.format(new Date(timestamp));
    }
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options); // 메뉴 리소스를 팝업 메뉴에 추가
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // 사용자가 '편집'을 선택한 경우
                editPost();
                return true;
            } else if (itemId == R.id.action_delete) {
                // 사용자가 '삭제'를 선택한 경우
                deletePost();
                return true;
            }
            // 다른 메뉴 아이템이 선택된 경우 기본 처리
            return false;
        });
        popup.show();
    }

    private void editPost() {
        Intent intentFromDetail = getIntent();
        String title = intentFromDetail.getStringExtra("title");
        String content = intentFromDetail.getStringExtra("content");
        String postId = intentFromDetail.getStringExtra("postId");

        ArrayList<String> photoUrlsString = getIntent().getStringArrayListExtra("photoUrls"); // 이미 있는 photoUrlsString을 직접 사용

        Intent intentToEdit = new Intent(CeoDetailActivity.this, CeoWriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId); // 여기에 postId를 다시 추가
        // 이미지 URI를 String으로 변환하여 ArrayList에 추가
        // 변환된 String 리스트를 인텐트에 추가
        if(photoUrlsString != null) {
            intentToEdit.putStringArrayListExtra("photoUrls", photoUrlsString);
        }
        startActivity(intentToEdit);
    }

    public void deletePost() {
        if (postId != null) {
            databaseReference.child(postId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CeoDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CeoDetailActivity.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(CeoDetailActivity.this, "오류: postId가 null입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}