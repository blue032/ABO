package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String userName; // 클래스의 멤버 변수로 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeboard); // 이 layout은 적절히 수정해야 할 수도 있습니다.

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(WriteBoardActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    Intent intent = new Intent(WriteBoardActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    Intent intent = new Intent(WriteBoardActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });

        editTextTitle = findViewById(R.id.editTextPostTitle); // layout의 id와 일치해야 합니다.
        editTextContent = findViewById(R.id.editTextPostContent); // layout의 id와 일치해야 합니다.
        buttonSubmit = findViewById(R.id.buttonSubmitPost); // layout의 id와 일치해야 합니다.

        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            userName = email != null ? email : "Anonymous"; // 이메일이 없는 경우 "Anonymous" 사용
        }
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (!title.isEmpty() && !content.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            BoardPost post = new BoardPost(title, content, timestamp, userName);
            databaseReference.push().setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(WriteBoardActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(WriteBoardActivity.this, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
