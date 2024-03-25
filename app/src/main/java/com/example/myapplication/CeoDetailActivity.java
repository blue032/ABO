package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.PopupMenu;

import java.util.List;

public class CeoDetailActivity extends AppCompatActivity {

    private EditText etComment; // 댓글 입력 필드 추가
    private DatabaseReference databaseReference;
    private String postId;
    private List<Comment> commentList; // 댓글 목록을 관리하는 리스트
    private CommentAdapter commentAdapter; // 댓글 어댑터
    private ImageView uploadedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_detail); // 새로운 레이아웃 파일 사용


        uploadedImageView = findViewById(R.id.uploadedImageView);
        // 게시물 ID 가져오기
        postId = getIntent().getStringExtra("postId");
        // Firebase 데이터베이스 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String photoUrl = intent.getStringExtra("photoUrl"); // 사진 URL 추가

        // 데이터로 뷰를 설정
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        tvTitle.setText(title);
        tvContent.setText(content);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Uri photoUri = Uri.parse(photoUrl);
            uploadedImageView.setImageURI(photoUri);
        }
        else {
            uploadedImageView.setVisibility(View.GONE);
        }
        // 더보기 아이콘 클릭 이벤트 설정
        iconMore.setOnClickListener(view -> showPopupMenu(view));
    }

    private void showPopupMenu(View view) {
        // 팝업 메뉴 생성 및 옵션 추가
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options);
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

    private void editPost() {
        // 인텐트로부터 제목, 내용, 게시물 ID, 사진을 가져옴
        Intent intentFromDetail = getIntent();
        String title = intentFromDetail.getStringExtra("title");
        String content = intentFromDetail.getStringExtra("content");
        String postId = intentFromDetail.getStringExtra("postId");
        String photoUrl = intentFromDetail.getStringExtra("photoUrl");

        // CeoWriteBoardActivity로 전환하는 인텐트를 생성하고 제목과 내용, 사진을 담음
        Intent intentToEdit = new Intent
                (CeoDetailActivity.this, CeoWriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true); // 편집 모드임을 나타냄
        intentToEdit.putExtra("postId", postId); // 게시물 ID 전달
        intentToEdit.putExtra("photoUrl", photoUrl);
        startActivity(intentToEdit);
    }

    public void deletePost() {
        // 게시글 삭제 로직
        if (postId != null) {
            databaseReference.child(postId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CeoDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    // CeoBoardActivity로 리디렉션
                    Intent intent = new Intent(CeoDetailActivity.this, CeoBoardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CeoDetailActivity.this, "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(CeoDetailActivity.this, "오류: postId가 null입니다.", Toast.LENGTH_SHORT).show();
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