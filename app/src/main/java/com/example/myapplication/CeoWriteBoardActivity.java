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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CeoWriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceowriteboard); // 이 layout은 적절히 수정해야 할 수도 있습니다.

        editTextTitle = findViewById(R.id.editTextPostTitle); // layout의 id와 일치해야 합니다.
        editTextContent = findViewById(R.id.editTextPostContent); // layout의 id와 일치해야 합니다.
        buttonSubmit = findViewById(R.id.buttonSubmitPost); // layout의 id와 일치해야 합니다.

        // Firebase Realtime Database의 'ceoBoard' 경로 참조
        databaseReference = FirebaseDatabase.getInstance().getReference("ceoBoard");

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(CeoWriteBoardActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoWriteBoardActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(CeoWriteBoardActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // 제목과 내용이 비어 있지 않은지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            // 현재 시간을 타임스탬프로 사용
            long timestamp = System.currentTimeMillis();

            // 여기서 CeoBoardPost 객체를 생성합니다.
            CeoBoardPost post = new CeoBoardPost(title, content, timestamp);
            // Firebase Realtime Database에 데이터 저장
            databaseReference.push().setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(CeoWriteBoardActivity.this, "게시글이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                } else {
                    Toast.makeText(CeoWriteBoardActivity.this, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
