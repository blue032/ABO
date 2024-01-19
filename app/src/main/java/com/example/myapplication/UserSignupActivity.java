package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserSignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersignup);

        // "back" 텍스트뷰에 대한 참조를 생성
        TextView backTextView = findViewById(R.id.back);

        // 비밀번호와 비밀번호 확인 EditText 참조 생성
        EditText passwordEditText = findViewById(R.id.signPW);
        EditText confirmPasswordEditText = findViewById(R.id.signPW2);

        // 다른 입력 필드들에 대한 참조
        EditText nameEditText = findViewById(R.id.signName);
        EditText idEditText = findViewById(R.id.signID);
        EditText birthText = findViewById(R.id.signBirth);
        EditText birth2Text = findViewById(R.id.signBirth2);
        EditText birth3Text = findViewById(R.id.signBirth3);
        EditText mailText = findViewById(R.id.signmail);

        // '확인' 버튼 참조 생성
        Button confirmButton = findViewById(R.id.pwcheckbutton);

        // '회원가입' 버튼 참조 생성
        Button signupButton = findViewById(R.id.signupbutton);

        // "back" 텍스트뷰 클릭 리스너 설정
        backTextView.setOnClickListener(v -> {
            // Signup1Activity로 이동하는 Intent 생성
            Intent intent = new Intent(UserSignupActivity.this, Signup1Activity.class);
            startActivity(intent);
        });

        // '확인' 버튼 클릭 리스너 설정
        confirmButton.setOnClickListener(v -> {
            // 비밀번호와 비밀번호 확인의 텍스트 값을 가져옴
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            // 비밀번호 일치 여부 확인
            if (password.equals(confirmPassword)) {
                Toast.makeText(UserSignupActivity.this, "일치합니다!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UserSignupActivity.this, "다시 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // '회원가입' 버튼 클릭 리스너 설정
        signupButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String id = idEditText.getText().toString();
            String signBirth = birthText.getText().toString();
            String signBirth2 = birth2Text.getText().toString();
            String signBirth3 = birth3Text.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String mail = mailText.getText().toString();


            // 모든 필드가 채워져 있는지 확인
            if (name.isEmpty() /* ... 다른 필드 검사 */ || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(UserSignupActivity.this, "모든 필드를 입력해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 비밀번호 확인
            if (!password.equals(confirmPassword)) {
                Toast.makeText(UserSignupActivity.this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase에 데이터 저장하는 로직 추가
            // ...

            // 저장 후 메시지 표시 및 로그인 화면으로 이동
            Toast.makeText(UserSignupActivity.this, "회원가입이 되었습니다!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(UserSignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 3000);
        });
    }
}
