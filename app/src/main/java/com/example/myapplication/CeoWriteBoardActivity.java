package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    }

    private void submitPost() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        // 제목과 내용이 비어 있지 않은지 확인
        if (!title.isEmpty() && !content.isEmpty()) {
            // 현재 시간을 타임스탬프로 사용
            long timestamp = System.currentTimeMillis();

            // BoardPost 객체 생성
            CeoBoardActivity.BoardPost post = new CeoBoardActivity.BoardPost(title, content, timestamp);

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
