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

public class CeoBoardActivity extends AppCompatActivity {
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
                    BoardPost post = snapshot.getValue(BoardPost.class);
                    if (post != null) {
                        if (post.getTimestamp() == 0) {
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

    public static class BoardPost {
        private String title;
        private String content;
        private long timestamp;

        public BoardPost() {
        }

        public BoardPost(String title, String content, long timestamp) {
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
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
    }

    private class BoardPostAdapter extends RecyclerView.Adapter<CeoBoardActivity.BoardPostAdapter.BoardPostViewHolder> {
        private ArrayList<CeoBoardActivity.BoardPost> postList;

        public BoardPostAdapter(ArrayList<CeoBoardActivity.BoardPost> postList) {
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
            BoardPost post = postList.get(position);
            holder.textViewTitle.setText(post.getTitle());
            holder.textViewDate.setText(post.getFormattedDate());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CeoBoardActivity.this, DetailActivity.class);
                    intent.putExtra("title", post.getTitle());
                    intent.putExtra("content", post.getContent());
                    intent.putExtra("timestamp", post.getTimestamp());
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
