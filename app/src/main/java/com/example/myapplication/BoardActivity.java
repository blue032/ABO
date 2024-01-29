package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BoardPostAdapter adapter;
    private ArrayList<BoardPost> postList;
    private DatabaseReference databaseReference;
    private TextView tvEmptyView; // TextView for empty list message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        FloatingActionButton fabAddPost = findViewById(R.id.fabAddPost);
        tvEmptyView = findViewById(R.id.tvEmptyView); // Initialize TextView for empty list

        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BoardActivity.this, WriteBoardActivity.class);
                startActivity(intent);
            }
        });

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new BoardPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        // Firebase Realtime Database 참조
        databaseReference = FirebaseDatabase.getInstance().getReference("board");

        // 데이터베이스에서 게시글 데이터 읽기
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BoardPost post = snapshot.getValue(BoardPost.class);
                    if (post != null) {
                        if (post.getTimestamp() == 0) {
                            // timestamp가 없는 경우 현재 시간을 설정합니다.
                            post.setTimestamp(System.currentTimeMillis());
                        }
                        postList.add(post);
                    }
                }
                checkForEmptyList();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 읽기 실패
            }
        });
    }

    // 데이터 목록이 비어있는지 확인하고 뷰의 가시성을 설정합니다.
    private void checkForEmptyList() {
        if (postList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // 게시글 데이터 모델
    public static class BoardPost {
        private String title;
        private String content;
        private long timestamp; // 작성 시간을 위한 타임스탬프

        public BoardPost() {
            // Firebase가 기본 생성자를 요구합니다.
        }

        public BoardPost(String title, String content) {
            this.title = title;
            this.content = content;
            this.timestamp = new Date().getTime(); // 현재 시간을 타임스탬프로 저장
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

        // 타임스탬프를 날짜/시간 문자열로 변환
        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    // 게시글 목록을 위한 RecyclerView Adapter
    // 이 부분은 어댑터의 구체적인 구현에 따라 다를 수 있습니다.
    private class BoardPostAdapter extends RecyclerView.Adapter<BoardPostAdapter.BoardPostViewHolder> {
        private ArrayList<BoardPost> postList;

        public BoardPostAdapter(ArrayList<BoardPost> postList) {
            this.postList = postList;
        }

        @NonNull
        @Override
        public BoardPostAdapter.BoardPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item_layout, parent, false);
            return new BoardPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BoardPostViewHolder holder, int position) {
            BoardPost post = postList.get(position);
            holder.textViewTitle.setText(post.getTitle());
            holder.textViewDate.setText(post.getFormattedDate());

            // 여기서 각 항목의 클릭 이벤트를 설정합니다.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 상세 페이지로 이동하는 인텐트를 생성합니다.
                    Intent intent = new Intent(BoardActivity.this, DetailActivity.class);
                    intent.putExtra("title", post.getTitle()); // 제목 전달
                    intent.putExtra("content", post.getContent()); // 내용 전달
                    intent.putExtra("timestamp", post.getTimestamp()); // 타임스탬프 전달
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        // 게시글 뷰 홀더
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
