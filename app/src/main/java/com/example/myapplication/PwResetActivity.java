package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PwResetActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button checkPasswordMatchButton, resetPasswordButton;
    private TextView passwordMatchTextView;
    private FirebaseAuth mAuth;

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
        if (user != null) {
            String newPassword = newPasswordEditText.getText().toString();

            // Firebase에서 비밀번호 재설정
            user.updatePassword(newPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 비밀번호가 성공적으로 변경된 경우
                            Toast.makeText(PwResetActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // 오류 처리
                            Toast.makeText(PwResetActivity.this, "비밀번호 변경 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
