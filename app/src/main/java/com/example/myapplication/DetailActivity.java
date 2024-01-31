package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private EditText etComment; // 댓글 입력 필드 추가
    private List<Comment> commentList; // 댓글 목록을 관리하는 리스트
    private CommentAdapter commentAdapter; // 댓글 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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
        commentList.add(comment);
        commentAdapter.notifyDataSetChanged();
        etComment.getText().clear(); // 댓글 입력 필드 초기화
    }
}
