package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private EditText etComment; // 댓글 입력 필드 추가
    private List<Comment> commentList; // 댓글 목록을 관리하는 리스트
    private CommentAdapter commentAdapter; // 댓글 어댑터
    private String postId;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //게시물 Id 가져오기
        postId = getIntent().getStringExtra("postId");
        //파베 레퍼런스 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Board");
        // 인텐트에서 데이터 가져오기
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        // 가져온 데이터로 뷰를 설정
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        tvTitle.setText(title);
        tvContent.setText(content);

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
        commentAdapter = new CommentAdapter(commentList);

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
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                // 게시물 수정 코드
                editPost();
                return true;
            } else if (id == R.id.action_delete) {
                // 게시물 삭제 코드
                deletePost();
                return true;
            }
            return false;
        });
        popup.show();
    }

    // 게시물을 수정하는 메서드
    private void editPost() {
        //인텐트로부터 제목, 내용, 게시물 id를 가져옴
        Intent intentFromDetail = getIntent();
        String title = intentFromDetail.getStringExtra("title");
        String content = intentFromDetail.getStringExtra("content");
        String postId = intentFromDetail.getStringExtra("postId");

        //WriteBoardActivity로 전환하는 인텐트를 생성하고 제목과 내용을 담음
        Intent intentToEdit = new Intent(DetailActivity.this, WriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId);
        startActivity(intentToEdit);
    }


    // DetailActivity의 deletePost 메서드 내
    public void deletePost() {
        // 게시글 삭제 로직
        if (postId != null) {
            databaseReference.child(postId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DetailActivity.this, BoardActivity.class);
                    //아래 플래그를 사용하여 BoardActivity로 돌아가면서 그 위의 모든 액티비티를 클리어함
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DetailActivity.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(DetailActivity.this,"오류: postId가 null입니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private void addComment(String commentText) {
        // 댓글 추가 로직
        // 예: 댓글을 데이터베이스에 추가하고 화면에 표시
        Comment comment = new Comment(commentText);
        commentList.add(comment);
        commentAdapter.notifyDataSetChanged();
        etComment.getText().clear(); // 댓글 입력 필드 초기화
    }
}