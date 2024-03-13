package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

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

    private EditText etComment; // 댓글 입력 필드 추가
    private List<Comment> commentList; // 댓글 목록을 관리하는 리스트
    private ArrayList<String> keyList; // 댓글 목록키 관리 리스트
    private CommentAdapter commentAdapter; // 댓글 어댑터

    private String key;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 인텐트에서 데이터 가져오기
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        key = getIntent().getStringExtra("key");
        // 가져온 데이터로 뷰를 설정
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        tvTitle.setText(title);
        tvContent.setText(content);

        databaseReference = FirebaseDatabase.getInstance().getReference("Board").child(key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                keyList.clear();
                commentList.clear();
                for (DataSnapshot comment : snapshot.child("Comment").getChildren()){

                    keyList.add(comment.getKey());

                    commentList.add(comment.getValue(Comment.class));

                    commentAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // 더보기 아이콘 클릭 이벤트 설정
        iconMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        // 댓글 입력 필드 초기화
        etComment = findViewById(R.id.etComment);

        // 댓글 목록 초기화
        commentList = new ArrayList<>();
        keyList = new ArrayList<>();
        commentAdapter = new CommentAdapter(databaseReference,commentList,keyList,this);

        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.recyclerViewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

        // 댓글 제출 버튼 클릭 이벤트 설정
        findViewById(R.id.btnSubmitComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 댓글 제출 로직 추가
                String commentText = etComment.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    // 댓글이 비어 있지 않으면 추가하는 로직을 구현
                    addComment(commentText);
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(DetailActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(DetailActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });
    }

    private void showPopupMenu(View view) {
        // 팝업 메뉴 생성 및 옵션 추가
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options); // 메뉴 리소스 파일
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_edit) {
                    // 게시물 수정 코드
                    editPost();
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    // 게시물 삭제 코드
                    deletePost();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void editPost() {
        // 게시물 수정 로직
        // 예: 편집 화면으로 인텐트 전송
    }

    private void deletePost() {
        // 게시물 삭제 로직
        // 예: 데이터베이스에서 게시물 삭제 및 사용자에게 알림 표시
    }

    private void addComment(String commentText) {
        // 댓글 추가 로직
        // 예: 댓글을 데이터베이스에 추가하고 화면에 표시
        Comment comment = new Comment(commentText);

        databaseReference.child("Comment").push().setValue(comment);

        etComment.getText().clear(); // 댓글 입력 필드 초기화
    }
}
