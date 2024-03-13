package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BoardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BoardPostAdapter adapter;
    private ArrayList<BoardPost> postList;
    private DatabaseReference databaseReference;
    private TextView tvEmptyView;
    private ArrayList<String> keyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        FloatingActionButton fabAddManagerPost = findViewById(R.id.fabAddPost);
        tvEmptyView = findViewById(R.id.tvEmptyView);

        fabAddManagerPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BoardActivity.this, WriteBoardActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        keyList = new ArrayList<>();
        adapter = new BoardPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                keyList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e("BoardActivity","snapshot = " + snapshot.getValue(BoardPost.class).getContent());
                    //자식키값들을 저장
                    keyList.add(snapshot.getKey());

                    BoardPost post = snapshot.getValue(BoardPost.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                Collections.sort(postList, new Comparator<BoardPost>() {
                    @Override
                    public int compare(BoardPost o1, BoardPost o2) {
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
            }
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
            String formattedDateWithUserName = post.getUserName() + " | " + post.getFormattedDate();
            holder.textViewDate.setText(formattedDateWithUserName);
            holder.textViewTitle.setText(post.getTitle());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BoardActivity.this, DetailActivity.class);
                    intent.putExtra("title", post.getTitle());
                    intent.putExtra("content", post.getContent());
                    intent.putExtra("timestamp", post.getTimestamp());
                    intent.putExtra("key", keyList.get(position));
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
