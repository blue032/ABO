package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
//게시판 처음 들어가면 보이는 곳에 대한 작동 (게시판 첫 화면)
public class CeoBoardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BoardPostAdapter adapter;
    private ArrayList<CeoBoardPost> postList;
    private DatabaseReference databaseReference;

    private BottomNavigationView bottomNavigationView;
    private TextView tvEmptyView;
    private ActivityResultLauncher<Intent> myActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceoboard);

        FloatingActionButton fabAddManagerPost = findViewById(R.id.fabAddPost);
        tvEmptyView = findViewById(R.id.tvEmptyView);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isCeo = prefs.getBoolean("IsCeo", false);

        if (isCeo) {
            fabAddManagerPost.setVisibility(View.VISIBLE);
        } else {
            fabAddManagerPost.setVisibility(View.GONE);
        }

        fabAddManagerPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CeoBoardActivity.this, CeoWriteBoardActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new BoardPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CeoBoardPost post = snapshot.getValue(CeoBoardPost.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey());
                        postList.add(post);
                    }
                }
                // 게시글 목록을 timestamp의 내림차순으로 정렬합니다.
                Collections.sort(postList, new Comparator<CeoBoardPost>() {
                    @Override
                    public int compare(CeoBoardPost o1, CeoBoardPost o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });
                checkForEmptyList();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setItemIconTintList(null);
        resetIcons(); // 초기 상태 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            resetIcons(); // 모든 아이콘을 회색으로 설정
            int itemId = item.getItemId();

            if (itemId == R.id.action_home) {
                item.setIcon(R.drawable.bottom_home_black);
                item.setChecked(true);// 선택된 아이콘으로 변경
                startActivity(new Intent(CeoBoardActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                item.setIcon(R.drawable.bottom_notification_black);
                item.setChecked(false);
                startActivity(new Intent(CeoBoardActivity.this, CeoBoardActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                item.setIcon(R.drawable.bottom_writeboard_black);
                item.setChecked(false);
                startActivity(new Intent(CeoBoardActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                item.setIcon(R.drawable.bottom_mypage_black);
                item.setChecked(false);
                startActivity(new Intent(CeoBoardActivity.this, MypageActivity.class));
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
                }
        );
    }

    private String formatTimestampToKST(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
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

    public static class CeoBoardPost {
        private String title;
        private String content;
        private long timestamp;
        private String postId;
        private List<String> photoUrls;
        private String userName;
        public CeoBoardPost() {
        }

        public CeoBoardPost(String title, String content, long timestamp, List<String> photoUrls, String userName) {
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
            this.photoUrls = photoUrls;
            this.userName = userName;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
        public String getPostId(){
            return postId;
        }
        public void setPostId(String postId){
            this.postId = postId;
        }
        public List<String> getPhotoUrls() {
            return photoUrls;
        }
        public void setPhotoUrls(List<String> photoUrls) {
            this.photoUrls = photoUrls;
        }
        public String getUserName() {
            return userName;
        }
        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    private class BoardPostAdapter extends RecyclerView.Adapter<BoardPostAdapter.BoardPostViewHolder> {
        private final ArrayList<CeoBoardPost> postList;

        public BoardPostAdapter(ArrayList<CeoBoardPost> postList) {
            this.postList = postList;  // 생성자에서 ArrayList를 받아 설정
        }

        @NonNull
        @Override
        public BoardPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new BoardPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BoardPostViewHolder holder, int position) {
            CeoBoardPost post = postList.get(position);
            holder.textViewTitle.setText(post.getTitle());

            String userName = post.getUserName();
            if (userName == null || userName.isEmpty()) {
                Log.e("CeoBoardActivity", "Invalid userName for post ID: " + post.getPostId());
                holder.textViewDate.setText(formatTimestampToKST(post.getTimestamp()) + " | Unknown user");
                return; // 중요: 여기서 함수를 끝내서 null userName으로 인한 오류를 방지합니다.
            }

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("CeoUsers").child(userName);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("Nickname")) {
                        String nickname = dataSnapshot.child("Nickname").getValue(String.class);
                        holder.textViewNickname.setText(nickname);
                        holder.textViewDate.setText(" | " + formatTimestampToKST(post.getTimestamp()));
                    } else {
                        holder.textViewNickname.setText("Unknown");
                        holder.textViewDate.setText(" |  " + formatTimestampToKST(post.getTimestamp()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching nickname: " + databaseError.getMessage());
                }
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(CeoBoardActivity.this, CeoDetailActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("title", post.getTitle());
                intent.putExtra("content", post.getContent());
                intent.putExtra("timestamp", post.getTimestamp());
                if (post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                    intent.putStringArrayListExtra("photoUrls", new ArrayList<>(post.getPhotoUrls()));
                }
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        class BoardPostViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTitle, textViewDate, textViewNickname;

            public BoardPostViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewDate = itemView.findViewById(R.id.textViewDate);
                textViewNickname = itemView.findViewById(R.id.textViewNickname);
            }
        }
    }
    private void resetIcons() {
        // 메뉴 아이템을 찾아 회색 아이콘으로 설정
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.action_home).setIcon(R.drawable.bottom_home_black);
        menu.findItem(R.id.action_notification).setIcon(R.drawable.bottom_notification_black);
        menu.findItem(R.id.action_board).setIcon(R.drawable.bottom_writeboard_black);
        menu.findItem(R.id.action_mypage).setIcon(R.drawable.bottom_mypage_black);
    }
    // 권한 거부 리스너 내부
}