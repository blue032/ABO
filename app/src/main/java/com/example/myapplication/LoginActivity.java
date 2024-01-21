package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Firebase Authentication
    private EditText emailEditText;
    private EditText passwordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase 인증 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 요소 참조
        emailEditText = findViewById(R.id.editID);
        passwordEditText = findViewById(R.id.editPassword);
        Button loginButton = findViewById(R.id.loginbutton);
        Button signInButton = findViewById(R.id.signin);

        // 로그인 버튼 클릭 리스너 설정
        loginButton.setOnClickListener(v -> loginUser());

        // 버튼에 클릭 리스너 설정
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인텐트를 생성하여 Signup1 액티비티를 시작합니다
                Intent intent = new Intent(LoginActivity.this, Signup1Activity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // id와 비밀번호 유효성 검사
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication을 사용하여 로그인
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공
                        Toast.makeText(LoginActivity.this, "로그인이 되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // 로그인 실패
                        Toast.makeText(LoginActivity.this, "다시 입력하세요" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
