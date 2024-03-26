package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
                Intent intent = new Intent(BoardActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_board) {
                // 게시판 아이템이 선택되었을 때의 동작 (현재 화면이 이미 BoardActivity이므로 아무 동작이 필요 없음)
                return true;
            } else if (itemId == R.id.action_notification) {
                // 알림 아이템이 선택되었을 때의 동작
                // 원하는 동작을 여기에 추가
                return true;
            } else if (itemId == R.id.action_mypage) {
                // 메뉴 페이지 아이템이 선택되었을 때의 동작
                Intent intent = new Intent(BoardActivity.this, MypageActivity.class);
                startActivity(intent);
                return true;
            }

            return false; // 아무 항목도 선택되지 않았을 경우
        });
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

    private static class BoardPostAdapter extends RecyclerView.Adapter<BoardPostAdapter.BoardPostViewHolder> {
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
            String formattedDateWithUserName = post.getUserName() + " | " + post.getFormattedDate();
            holder.textViewDate.setText(formattedDateWithUserName);
            holder.textViewTitle.setText(post.getTitle());

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("title", post.getTitle());
                intent.putExtra("content", post.getContent());
                intent.putExtra("timestamp", post.getTimestamp());
                intent.putExtra("postId", post.getPostId()); // 게시글 ID를 인텐트에 추가
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        static class BoardPostViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTitle, textViewDate;

            public BoardPostViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewDate = itemView.findViewById(R.id.textViewDate);
            }
        }
    }
}
