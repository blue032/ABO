package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PwResetActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button checkPasswordMatchButton, resetPasswordButton;
    private TextView passwordMatchTextView;
    private FirebaseAuth mAuth;
    private String userEmail, userId;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pw_reset);

        // Firebase 인증 초기화
        mAuth = FirebaseAuth.getInstance();

        // 레이아웃 요소 초기화
        newPasswordEditText = findViewById(R.id.pwreset);
        confirmPasswordEditText = findViewById(R.id.pwreset2);
        checkPasswordMatchButton = findViewById(R.id.pwcheckbutton);
        resetPasswordButton = findViewById(R.id.signin);
        passwordMatchTextView = findViewById(R.id.pwcheckbutton);

        userEmail = getIntent().getStringExtra("userEmail");
        userId = getIntent().getStringExtra("userId");

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // 입력 상태에 따라 버튼 활성화 상태 업데이트
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        newPasswordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);

        // "일치" 버튼 클릭 시 비밀번호 일치 여부 확인
        checkPasswordMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordMatch();
            }
        });

        // 비밀번호 재설정 버튼 클릭 시 비밀번호 변경
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void checkInputs() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // 입력된 새 비밀번호와 비밀번호 재입력이 일치하면 "일치" 버튼 활성화
        checkPasswordMatchButton.setEnabled(newPassword.equals(confirmPassword));
    }

    private void checkPasswordMatch() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (newPassword.equals(confirmPassword)) {
            passwordMatchTextView.setText("일치!");
        } else {
            passwordMatchTextView.setText("불일치");
        }
    }

    private void resetPassword() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail().equals(userEmail)) {
            String newPassword = newPasswordEditText.getText().toString();

            // Firebase에서 비밀번호 재설정
            user.updatePassword(newPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Firebase Auth에 비밀번호 업데이트 성공 후, Realtime Database에도 저장
                            savePasswordInDatabase(userId, newPassword);
                            Toast.makeText(PwResetActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e("PasswordResetError", "비밀번호 변경 실패: " + task.getException().getMessage());
                            Toast.makeText(PwResetActivity.this, "비밀번호 변경 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void savePasswordInDatabase(String userId, String newPassword) {
        // 사용자의 비밀번호를 Realtime Database에 저장
        databaseReference.child(userId).child("password").setValue(newPassword)
                .addOnSuccessListener(aVoid -> {
                    // 비밀번호 데이터베이스 업데이트 성공
                })
                .addOnFailureListener(e -> {
                    // 데이터베이스 업데이트 실패
                });
    }
}
