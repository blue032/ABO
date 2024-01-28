package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WriteBoardActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Button buttonSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writeboard);

        // Firebase Realtime Database 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("board");

        // UI 컴포넌트 초기화
        editTextTitle = findViewById(R.id.editTextPostTitle);
        editTextContent = findViewById(R.id.editTextPostContent);
        buttonSubmit = findViewById(R.id.buttonSubmitPost);

        // 제출 버튼 클릭 리스너
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // 제목과 내용이 모두 입력되었는지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            BoardPost post = new BoardPost(title, content);

            // Firebase Realtime Database에 데이터 저장
            databaseReference.push().setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        // 성공적으로 데이터를 저장했을 때의 동작
                        Toast.makeText(WriteBoardActivity.this, "게시글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        finish(); // 현재 액티비티 종료
                    })
                    .addOnFailureListener(e -> {
                        // 데이터 저장 실패 시
                        Toast.makeText(WriteBoardActivity.this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 게시판 데이터 모델
    private static class BoardPost {
        private String title;
        private String content;

        public BoardPost() {
            // Firebase가 기본 생성자를 요구합니다.
        }

        public BoardPost(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
