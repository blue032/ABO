package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.PopupMenu;

public class CeoDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private String postId;
    private ImageView uploadedImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_detail);

        uploadedImageView = findViewById(R.id.uploadedImageView);
        postId = getIntent().getStringExtra("postId");
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String photoUrl = intent.getStringExtra("photoUrl");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        tvTitle.setText(title);
        tvContent.setText(content);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            Uri photoUri = Uri.parse(photoUrl);
            uploadedImageView.setImageURI(photoUri);
        } else {
            uploadedImageView.setVisibility(View.GONE);
        }

        iconMore.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options); // 메뉴 리소스를 팝업 메뉴에 추가
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // 사용자가 '편집'을 선택한 경우
                editPost();
                return true;
            } else if (itemId == R.id.action_delete) {
                // 사용자가 '삭제'를 선택한 경우
                deletePost();
                return true;
            }
            // 다른 메뉴 아이템이 선택된 경우 기본 처리
            return false;
        });
        popup.show();
    }


    private void editPost() {
        Intent intentFromDetail = getIntent();
        String title = intentFromDetail.getStringExtra("title");
        String content = intentFromDetail.getStringExtra("content");
        String postId = intentFromDetail.getStringExtra("postId");
        String photoUrl = intentFromDetail.getStringExtra("photoUrl");

        Intent intentToEdit = new Intent(CeoDetailActivity.this, CeoWriteBoardActivity.class);
        intentToEdit.putExtra("title", title);
        intentToEdit.putExtra("content", content);
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId);
        intentToEdit.putExtra("photoUrl", photoUrl);
        startActivity(intentToEdit);
    }

    public void deletePost() {
        if (postId != null) {
            databaseReference.child(postId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CeoDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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
}
