package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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

        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(CeoBoardActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoBoardActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    startActivity(new Intent(CeoBoardActivity.this, NotificationActivity.class));
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoBoardActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false; // 아무 항목도 선택되지 않았을 경우
            }
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

    private class BoardPostAdapter extends RecyclerView.Adapter<CeoBoardActivity.BoardPostAdapter.BoardPostViewHolder> {
        private final ArrayList<CeoBoardPost> postList;

        public BoardPostAdapter(ArrayList<CeoBoardPost> postList) {
            this.postList = postList;
        }

        @NonNull
        @Override
        public CeoBoardActivity.BoardPostAdapter.BoardPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new CeoBoardActivity.BoardPostAdapter.BoardPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CeoBoardActivity.BoardPostAdapter.BoardPostViewHolder holder, int position) {
            CeoBoardPost post = postList.get(position);
            holder.textViewTitle.setText(post.getTitle());
            holder.textViewDate.setText(post.getFormattedDate());

            //클릭 리스너 설정
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //수정하려는 게시글의 정보를 인텐트에 담음
                    Intent intent = new Intent(CeoBoardActivity.this, CeoDetailActivity.class);
                    intent.putExtra("postId", post.getPostId()); // 게시글 ID를 인텐트에 추가
                    intent.putExtra("title", post.getTitle());
                    intent.putExtra("content", post.getContent());
                    intent.putExtra("timestamp", post.getTimestamp());

                    if (post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                        // List<String>을 ArrayList<String>으로 변환
                        ArrayList<String> photoUrls = new ArrayList<>(post.getPhotoUrls());
                        intent.putStringArrayListExtra("photoUrls", photoUrls);
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