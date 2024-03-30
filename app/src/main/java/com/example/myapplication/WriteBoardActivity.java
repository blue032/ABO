package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;
    private String userName; // 클래스의 멤버 변수로 선언
    private boolean isEditing;
    private String postId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeboard); // 이 layout은 적절히 수정해야 할 수도 있습니다.

        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);

        // 인텐트에서 데이터 추출
        Intent intent = getIntent();
        isEditing = intent.getBooleanExtra("isEditing", false);
        // 수정 모드일 때만 제목과 내용, postId를 인텐트에서 가져와서 설정
        if (isEditing) {
            editTextTitle.setText(intent.getStringExtra("title"));
            editTextContent.setText(intent.getStringExtra("content"));
            postId = intent.getStringExtra("postId"); // postId를 멤버 변수에 저장
        }


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
                    startActivity(new Intent(WriteBoardActivity.this, NotificationActivity.class));
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
        databaseReference = FirebaseDatabase.getInstance().getReference("Board");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    // 게시물을 제출하거나 수정하는 메서드
    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // 제목과 내용이 비어 있지 않은지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            // 게시물 객체 생성
            BoardPost post = new BoardPost(title, content, System.currentTimeMillis(), userName);

            // 수정 모드인 경우 postId가 있을 것이고, 그 값을 사용하여 게시물 업데이트
            if (isEditing && postId != null) {
                // postId를 사용하여 해당 게시물을 데이터베이스에서 찾고 업데이트
                databaseReference.child(postId).setValue(post)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 수정완료 후 BoardActivity로 이동
                                Toast.makeText(WriteBoardActivity.this, "게시글이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(WriteBoardActivity.this, BoardActivity.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 종료
                            } else {
                                // 실패하면 에러 메시지 표시
                                Toast.makeText(WriteBoardActivity.this, "게시글 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // 새 게시물 추가 로직
                // 데이터베이스의 'Board' 노드에 새로운 게시물을 추가
                databaseReference.push().setValue(post)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 성공적으로 추가되면 사용자에게 알림
                                Toast.makeText(WriteBoardActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                                //추가온료 후 BoardActivity로 이동
                                Intent intent = new Intent(WriteBoardActivity.this, BoardActivity.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 종료
                            } else {
                                // 실패하면 에러 메시지 표시
                                Toast.makeText(WriteBoardActivity.this, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            // 제목이나 내용이 비어 있으면 사용자에게 알림
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
