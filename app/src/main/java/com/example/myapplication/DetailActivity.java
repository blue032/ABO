package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        postId = getIntent().getStringExtra("postId");
        commentReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        // 데이터베이스 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        tvTitle.setText(title);
        tvContent.setText(content);

        iconMore.setOnClickListener(view -> showPopupMenu(view));

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
        String commentId = commentReference.push().getKey();
        Comment comment = new Comment(commentText);
        if (commentId != null) {
            commentReference.child(commentId).setValue(comment);
            etComment.getText().clear();
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
                Toast.makeText(DetailActivity.this, "Failed to load comments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                editPost();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
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

        Intent intentToEdit = new Intent(DetailActivity.this, WriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId);
        startActivity(intentToEdit);
    }

    public void deletePost() {
        if (postId != null) {
            commentReference.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "게시글이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 삭제 후 액티비티 종료
                } else {
                    Toast.makeText(DetailActivity.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(DetailActivity.this, "오류: postId가 null입니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
