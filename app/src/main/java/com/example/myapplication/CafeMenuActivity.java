package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CafeMenuActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CafeMenuPostAdapter adapter;
    private ArrayList<CafeMenuPost> postList;
    private DatabaseReference databaseReference;
    private FloatingActionButton fabAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafemenu);

        recyclerView = findViewById(R.id.recyclerViewPosts);
        fabAddPost = findViewById(R.id.fabAddPost);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new CafeMenuPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("OOcafemenu");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CafeMenuPost post = snapshot.getValue(CafeMenuPost.class);
                    if (post != null) {
                        post.setPostId(snapshot.getKey());
                        Log.d("CafeMenuActivity", "Loaded post: " + post.getTitle());
                        postList.add(post);
                    }
                }
                Collections.sort(postList, new Comparator<CafeMenuPost>() {
                    @Override
                    public int compare(CafeMenuPost o1, CafeMenuPost o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isCeo = prefs.getBoolean("IsCeo", false);

        if (isCeo) {
            fabAddPost.setVisibility(View.VISIBLE);
        } else {
            fabAddPost.setVisibility(View.GONE);
        }

        fabAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CafeMenuActivity.this, CafeMenu_WriteBoard.class);
                startActivity(intent);
            }
        });
    }

    private static class CafeMenuPost {
        private String title;
        private String content;
        private List<String> photoUrls;
        private String postId;
        private long timestamp;

        public CafeMenuPost() {
            // Default constructor for Firebase and other usages
        }

        // Getters and setters for the properties
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

        public List<String> getPhotoUrls() {
            return photoUrls;
        }

        public void setPhotoUrls(List<String> photoUrls) {
            this.photoUrls = photoUrls;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }


    private class CafeMenuPostAdapter extends RecyclerView.Adapter<CafeMenuPostAdapter.CafeMenuPostViewHolder> {
        private final ArrayList<CafeMenuPost> postList;

        public CafeMenuPostAdapter(ArrayList<CafeMenuPost> postList) {
            this.postList = postList;
        }

        @NonNull
        @Override
        public CafeMenuPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cafemenu_post_item_layout, parent, false);
            return new CafeMenuPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CafeMenuPostViewHolder holder, int position) {
            CafeMenuPost post = postList.get(position);
            holder.bind(post);
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }

        class CafeMenuPostViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            ImageView photoImageView;

            CafeMenuPostViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.textViewTitle);
                photoImageView = itemView.findViewById(R.id.imageView);
            }

            void bind(final CafeMenuPost post) {
                titleTextView.setText(post.getTitle());
                // 이미지 URL 리스트가 null이 아니고 비어 있지 않은 경우에만 이미지를 로드
                if (post.getPhotoUrls() != null && !post.getPhotoUrls().isEmpty()) {
                    photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(post.getPhotoUrls().get(0))
                            .into(photoImageView);
                } else {
                    photoImageView.setVisibility(View.GONE);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 상세 활동을 시작하기 위한 인텐트를 생성합니다.
                        Intent intent = new Intent(view.getContext(), CafeMenu_Detail.class);
                        intent.putExtra("postID", post.getPostId()); // 게시글 ID
                        intent.putExtra("title", post.getTitle()); // 게시글 제목
                        intent.putExtra("content", post.getContent()); // 게시글 내용

                        // 사진 URL 리스트가 null이 아닌지 확인하고, null이면 빈 리스트를 전달
                        ArrayList<String> photoUrls = post.getPhotoUrls() != null ? new ArrayList<>(post.getPhotoUrls()) : new ArrayList<>();
                        intent.putStringArrayListExtra("photoUrls", photoUrls);

                        // 인텐트를 사용하여 CafeMenu_Detail 활동을 시작합니다.
                        view.getContext().startActivity(intent);
                    }
                });
            }

        }



    }
}
