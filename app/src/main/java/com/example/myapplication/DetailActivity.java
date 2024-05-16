package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
import java.util.Date;
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
    private String postOwnerId;
    private ImageView iconMore;
    private DatabaseReference databaseReference;
    private RecyclerView imagesRecyclerView;
    private ArrayList<String> photoUrls; // 사진 URL을 저장할 멤버 변수 선언


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        postId = getIntent().getStringExtra("postId");
        databaseReference = FirebaseDatabase.getInstance().getReference("Board");
        postReference = FirebaseDatabase.getInstance().getReference("Board").child(postId);
        commentReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        final TextView tvTitle = findViewById(R.id.tvTitle);
        final TextView tvContent = findViewById(R.id.tvContent);
        final TextView tvNickname = findViewById(R.id.tvnickname);
        final TextView tvTimestamp = findViewById(R.id.tvTimestamp);

        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postOwnerId = snapshot.child("ownerId").getValue(String.class);
                    postUserName = snapshot.child("userName").getValue(String.class);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(postUserName);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                String nickname = userSnapshot.child("Nickname").getValue(String.class);
                                tvNickname.setText(nickname);
                            } else {
                                // Users에서 찾을 수 없다면 CeoUsers에서 찾기
                                DatabaseReference ceoUserRef = FirebaseDatabase.getInstance().getReference("CeoUsers").child(postUserName);
                                ceoUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot ceoUserSnapshot) {
                                        if (ceoUserSnapshot.exists()) {
                                            String nickname = ceoUserSnapshot.child("Nickname").getValue(String.class);
                                            tvNickname.setText(nickname);
                                        } else {
                                            Toast.makeText(DetailActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(DetailActivity.this, "사용자 정보 조회에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(DetailActivity.this, "사용자 정보 조회에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    String title = snapshot.child("title").getValue(String.class);
                    String content = snapshot.child("content").getValue(String.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    tvTitle.setText(title);
                    tvContent.setText(content);
                    tvTimestamp.setText(formatTimestampToKST(timestamp));

                    ArrayList<String> photoUrlsString = new ArrayList<>();
                    for (DataSnapshot photoSnapshot : snapshot.child("photoUrls").getChildren()) {
                        String photoUrl = photoSnapshot.getValue(String.class);
                        photoUrlsString.add(photoUrl);
                    }

                    ArrayList<Uri> photoUris = new ArrayList<>();
                    if (photoUrlsString != null) {
                        for (String photoUrl : photoUrlsString) {
                            photoUris.add(Uri.parse(photoUrl));
                        }
                    }

                    imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
                    imagesRecyclerView.setLayoutManager(new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    ImageAdapter adapter = new ImageAdapter(DetailActivity.this, photoUris);
                    imagesRecyclerView.setAdapter(adapter);
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


        // 여기에 iconMore 초기화와 클릭 이벤트 리스너를 추가합니다.
        iconMore = findViewById(R.id.iconMore);
        iconMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
    }


    private String formatTimestampToKST(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 한국 시간대로 설정
        return sdf.format(new Date(timestamp));
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                editPost();
                return true;
            } else if (id == R.id.action_delete) {
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
        String postId = intentFromDetail.getStringExtra("postId");

        ArrayList<String> photoUrlsString = getIntent().getStringArrayListExtra("photoUrls"); // 이미 있는 photoUrlsString을 직접 사용

        Intent intentToEdit = new Intent(DetailActivity.this, WriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId); // 여기에 postId를 다시 추가
        // 이미지 URI를 String으로 변환하여 ArrayList에 추가
        // 변환된 String 리스트를 인텐트에 추가
        if (photoUrlsString != null) {
            intentToEdit.putStringArrayListExtra("photoUrls", photoUrlsString);
        }
        startActivity(intentToEdit);
    }


    private void deletePost() {
        if (postId != null) {
            postReference.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DetailActivity.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(DetailActivity.this, "오류: 게시글 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addComment(String commentText) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Board").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                .addOnFailureListener(e -> Toast.makeText(DetailActivity.this, "댓글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "데이터베이스 오류가 발생했습니다: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotification(String commentContent, String commenterId, long timestamp, String postId, String commentId, String postUserName) {
        String notificationId = notificationsReference.push().getKey();
        Notification notification = new Notification(notificationId, commentContent, postId, commentId, postOwnerId, commenterId, timestamp, false, postUserName);

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

                // Check if there are comments and set visibility accordingly
                if (commentList.isEmpty()) {
                    findViewById(R.id.imageView3).setVisibility(View.GONE);
                    findViewById(R.id.tvcommentet).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.imageView3).setVisibility(View.VISIBLE);
                    findViewById(R.id.tvcommentet).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "댓글을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}