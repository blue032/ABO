package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private EditText etComment;
    private List<Comment> commentList;
    private ArrayList<String> keyList;
    private CommentAdapter commentAdapter;
    private String postId;
    private DatabaseReference commentReference;
    private DatabaseReference notificationsReference;
    private DatabaseReference postReference;
    private String postOwnerName;
    private String postUserName;
    private String postOwnerId; // 게시글 소유자 ID를 클래스 변수로 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        postId = getIntent().getStringExtra("postId");

        postReference = FirebaseDatabase.getInstance().getReference("Board").child(postId);
        commentReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        final TextView tvTitle = findViewById(R.id.tvTitle);
        final TextView tvContent = findViewById(R.id.tvContent);

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postOwnerId = snapshot.child("ownerId").getValue(String.class);
                    postOwnerName = snapshot.child("ownerName").getValue(String.class);
                    postUserName = snapshot.child("userName").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    String content = snapshot.child("content").getValue(String.class);
                    tvTitle.setText(title);
                    tvContent.setText(content);
                } else {
                    Toast.makeText(DetailActivity.this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "게시글 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        etComment = findViewById(R.id.etComment);
        commentList = new ArrayList<>();
        keyList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentReference, commentList, keyList, this);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

        findViewById(R.id.btnSubmitComment).setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addComment(commentText);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_home) {
                startActivity(new Intent(DetailActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(DetailActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                startActivity(new Intent(DetailActivity.this, NotificationActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(DetailActivity.this, MypageActivity.class));
                return true;
            }

            return false;
        });

        loadComments();
    }

    private void addComment(String commentText) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String postUserName = snapshot.child("userName").getValue(String.class);
                    Comment comment = new Comment(commentText, userId, timestamp, postId, postUserName);
                    String commentId = commentReference.push().getKey();
                    if (commentId != null) {
                        commentReference.child(commentId).setValue(comment)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DetailActivity.this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                    etComment.getText().clear();
                                    createNotification(commentText, userId, timestamp, postId, commentId, postUserName);
                                })
                                .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(DetailActivity.this, "게시글을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "게시글 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotification(String commentContent, String commenterId, long timestamp, String postId, String commentId, String postUserName) {
        String notificationId = notificationsReference.push().getKey();
        Notification notification = new Notification(commentContent, postId, commentId, postOwnerId, commenterId, timestamp, false, postUserName);

        if (notificationId != null) {
            notificationsReference.child(notificationId).setValue(notification);
        }
    }

    private void loadComments() {
        commentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                keyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                    keyList.add(snapshot.getKey());
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "댓글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
