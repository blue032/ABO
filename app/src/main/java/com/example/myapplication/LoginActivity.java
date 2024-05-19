package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Firebase Authentication
    private EditText emailEditText;
    private EditText passwordEditText;
    private String actualPassword = "";

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
        TextView signInButton = findViewById(R.id.signin);
        TextView idTextView = findViewById(R.id.id);
        TextView pwTextView = findViewById(R.id.pw);

        // 텍스트뷰에서 특정 텍스트 색상 변경
        TextView textViewCertainU = findViewById(R.id.textViewCertainU);
        String text = "CertaIN U";
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf("IN U");
        int end = start + "IN U".length();
        int customBlueColor = getResources().getColor(R.color.lightBlue);
        spannableString.setSpan(new ForegroundColorSpan(customBlueColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewCertainU.setText(spannableString);


        // 로그인 버튼 클릭 리스너 설정
        loginButton.setOnClickListener(v -> loginUser());

        // 회원가입 및 ID/PW 찾기 버튼에 대한 클릭 리스너 설정
        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
            intent.putExtra("actionType", "signup");
            startActivity(intent);
        });

        idTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
            intent.putExtra("actionType", "findId");
            startActivity(intent);
        });

        pwTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SelectionActivity.class);
            intent.putExtra("actionType", "findPw");
            startActivity(intent);
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
                        // 로그인 성공 후 CeoUsers 경로에서 이메일 확인
                        checkIfCeo(email);
                    } else {
                        // 로그인 실패
                        Toast.makeText(LoginActivity.this, "다시 입력하세요" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfCeo(String email) {
        DatabaseReference ceoRef = FirebaseDatabase.getInstance().getReference("CeoUsers");
        ceoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isCeo = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String ceoEmail = snapshot.child("email").getValue(String.class);
                    if (ceoEmail != null && ceoEmail.equals(email)) {
                        isCeo = true;
                        break;
                    }
                }

                // SharedPreferences에 사장님 여부 저장
                SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                editor.putBoolean("IsCeo", isCeo);
                editor.apply();

                // 로그인 성공 후 메인 액티비티로 이동
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 에러 처리
                Toast.makeText(LoginActivity.this, "데이터베이스 에러: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String passwordMask(String password) {
        StringBuilder maskedPassword = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            maskedPassword.append('*'); // 각 문자를 별표로 대체
        }
        return maskedPassword.toString();
    }
}
