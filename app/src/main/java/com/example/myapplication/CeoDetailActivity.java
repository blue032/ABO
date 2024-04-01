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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.PopupMenu;

public class CeoDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private String postId;
    private ImageView uploadedImageView;
    private TextView tvTitle;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceo_detail);

        uploadedImageView = findViewById(R.id.uploadedImageView);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        ImageView iconMore = findViewById(R.id.iconMore);

        postId = getIntent().getStringExtra("postId");
        if (postId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard").child(postId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String title = dataSnapshot.child("title").getValue(String.class);
                        String content = dataSnapshot.child("content").getValue(String.class);
                        String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

                        tvTitle.setText(title);
                        tvContent.setText(content);

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Uri photoUri = Uri.parse(photoUrl);
                            uploadedImageView.setImageURI(photoUri);
                        } else {
                            uploadedImageView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(CeoDetailActivity.this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "오류: 게시물 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // postId가 없으면 활동을 종료합니다.
        }

        iconMore.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_edit_options);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                editPost();
                return true;
            } else if (id == R.id.action_delete) {
                deletePost();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editPost() {
        Intent intentToEdit = new Intent(CeoDetailActivity.this, CeoWriteBoardActivity.class);
        intentToEdit.putExtra("title", tvTitle.getText().toString());
        intentToEdit.putExtra("content", tvContent.getText().toString());
        intentToEdit.putExtra("isEditing", true);
        intentToEdit.putExtra("postId", postId);
        // 여기서는 photoUrl을 직접 전달하지 않고, 필요하다면 수정 화면에서 다시 불러와야 합니다.
        startActivity(intentToEdit);
    }

    public void deletePost() {
        if (postId != null) {
            databaseReference.removeValue().addOnCompleteListener(task -> {
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
