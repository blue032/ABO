package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BoardPostAdapter adapter;
    private ArrayList<BoardPost> postList;
    private DatabaseReference databaseReference;
    private TextView tvEmptyView;

    private ActivityResultLauncher<Intent> myActivityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        FloatingActionButton fabAddManagerPost = findViewById(R.id.fabAddPost);
        tvEmptyView = findViewById(R.id.tvEmptyView);

        fabAddManagerPost.setOnClickListener(view -> {
            Intent intent = new Intent(BoardActivity.this, WriteBoardActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new BoardPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BoardPost post = snapshot.getValue(BoardPost.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey()); // 게시물 객체에 ID 설정
                        postList.add(post);
                    }
                }
                Collections.sort(postList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                checkForEmptyList();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BoardActivity.this, "Failed to load posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // BottomNavigationView 설정

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                startActivity(new Intent(BoardActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(BoardActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                startActivity(new Intent(BoardActivity.this, NotificationActivity.class)); // 알림 항목 클릭 시 NotificationActivity로 이동
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(BoardActivity.this, MypageActivity.class));
                return true;
            }
            return false;
        });

        //ceowriteboardactivity에서 사진 받기 위해 사용
        myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String photoUriString = result.getData().getStringExtra("photoUri");
                        Uri photoUri = Uri.parse(photoUriString);
                        //uploadedPhoto.setImageURI(photoUri); // 'uploadedphoto'는 이미지를 표시할 ImageView
                    }
                });

    }

    private String formatTimestampToKST(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 한국 시간대로 설정
        return sdf.format(new Date(timestamp));
    }

    private void checkForEmptyList() {
        if (postList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class BoardPostAdapter extends RecyclerView.Adapter<BoardPostAdapter.BoardPostViewHolder> {
        private final ArrayList<BoardPost> postList;

        public BoardPostAdapter(ArrayList<BoardPost> postList) {
            this.postList = postList;
        }

        @NonNull
        @Override
        public BoardPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new BoardPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BoardPostViewHolder holder, int position) {
            BoardPost post = postList.get(position);
            holder.textViewTitle.setText(post.getTitle());

            // 닉네임을 가져오기 위해 Users 데이터베이스를 먼저 확인
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserName());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("Nickname")) {
                        String nickname = dataSnapshot.child("Nickname").getValue(String.class);
                        holder.textViewDate.setText(formatTimestampToKST(post.getTimestamp()) + " | " + nickname);
                    } else {
                        // Users에서 찾지 못했다면, CeoUsers 데이터베이스를 확인
                        DatabaseReference ceoUserRef = FirebaseDatabase.getInstance().getReference("CeoUsers").child(post.getUserName());
                        ceoUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot ceoSnapshot) {
                                if (ceoSnapshot.exists() && ceoSnapshot.hasChild("Nickname")) {
                                    String ceoNickname = ceoSnapshot.child("Nickname").getValue(String.class);
                                    holder.textViewDate.setText(formatTimestampToKST(post.getTimestamp()) + " | " + ceoNickname);
                                } else {
                                    holder.textViewDate.setText(formatTimestampToKST(post.getTimestamp()) + " | Unknown");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("Firebase", "Error fetching nickname from CeoUsers");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching nickname from Users");
                }
            });

            holder.textViewTitle.setText(post.getTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Intent to open DetailActivity with post details
                    Intent intent = new Intent(BoardActivity.this, DetailActivity.class);
                    intent.putExtra("postId", post.getPostId());
                    intent.putExtra("title", post.getTitle());
                    intent.putExtra("content", post.getContent());
                    if (post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                        intent.putStringArrayListExtra("photoUrls", new ArrayList<>(post.getPhotoUrls()));
                    }
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        class BoardPostViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTitle, textViewDate;

            public BoardPostViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewDate = itemView.findViewById(R.id.textViewDate);
            }
        }
    }
}