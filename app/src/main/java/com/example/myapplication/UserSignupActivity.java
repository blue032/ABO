package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserSignupActivity extends AppCompatActivity {

    private boolean isPasswordConfirmed = false; // 비밀번호 확인 여부를 저장할 변수
    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersignup);

        // Firebase 인증 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 요소 참조
        TextView backTextView = findViewById(R.id.back);
        EditText passwordEditText = findViewById(R.id.signPW);
        EditText confirmPasswordEditText = findViewById(R.id.signPW2);
        EditText NicknameEditText = findViewById(R.id.signNickName);
        EditText nameEditText = findViewById(R.id.signName);
        EditText phoneEditText = findViewById(R.id.signPhone);
        EditText birthText = findViewById(R.id.signBirth);
        EditText birth2Text = findViewById(R.id.signBirth2);
        EditText birth3Text = findViewById(R.id.signBirth3);
        EditText mailText = findViewById(R.id.signmail);
        Button confirmButton = findViewById(R.id.pwcheckbutton);
        Button signupButton = findViewById(R.id.signupbutton);

        // "back" 텍스트뷰 클릭 이벤트
        backTextView.setOnClickListener(v -> {
            Intent intent = new Intent(UserSignupActivity.this, SelectionActivity.class);
            startActivity(intent);
        });

        // '확인' 버튼 클릭 이벤트
        confirmButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (password.equals(confirmPassword)) {
                Toast.makeText(UserSignupActivity.this, "일치합니다!", Toast.LENGTH_SHORT).show();
                isPasswordConfirmed = true;
            } else {
                Toast.makeText(UserSignupActivity.this, "다시 입력하세요", Toast.LENGTH_SHORT).show();
                isPasswordConfirmed = false;
            }
        });

        // '회원가입' 버튼 클릭 이벤트
        signupButton.setOnClickListener(v -> {
            String email = mailText.getText().toString().trim(); // 사용자 이메일
            String password = passwordEditText.getText().toString().trim(); // 사용자 비밀번호

            // 기타 필드 데이터
            String name = nameEditText.getText().toString();
            String Nickname = NicknameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String signBirth = birthText.getText().toString();
            String signBirth2 = birth2Text.getText().toString();
            String signBirth3 = birth3Text.getText().toString();

            // 생년월일을 하나의 문자열로 합치기
            String birth = signBirth + "-" + signBirth2 + "-" + signBirth3;

            // 입력 데이터 검증
            if (name.isEmpty() || phone.isEmpty() || birth.isEmpty() || password.isEmpty() || email.isEmpty() || !isPasswordConfirmed) {
                Toast.makeText(UserSignupActivity.this, "모든 필드를 입력하고 비밀번호를 확인해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Authentication을 사용하여 사용자 등록
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser(); // 현재 인증된 사용자의 정보를 가져옵니다.
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid(); // 사용자 UID를 가져옵니다.
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

                                User user = new User(name, Nickname, phone, password, birth, email);
                                databaseReference.child(uid).setValue(user) // UID를 키로 사용하여 데이터베이스에 사용자 정보 저장
                                        .addOnCompleteListener(taskDb -> {
                                            if (taskDb.isSuccessful()) {
                                                Toast.makeText(UserSignupActivity.this, "회원가입이 되었습니다!", Toast.LENGTH_SHORT).show();
                                                // 로그인 화면으로 이동
                                                new Handler().postDelayed(() -> {
                                                    Intent intent = new Intent(UserSignupActivity.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }, 2500);
                                            } else {
                                                Toast.makeText(UserSignupActivity.this, "데이터베이스 오류: " + taskDb.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // Authentication 등록 실패 처리
                            Toast.makeText(UserSignupActivity.this, "Authentication 오류: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // User 클래스
    public static class User {
        public String name, Nickname, phone, password, birth, mail;

        public User(String name, String Nickname, String phone, String password, String birth, String mail) {
            this.name = name;
            this.Nickname = Nickname;
            this.phone = phone;
            this.password = password;
            this.birth = birth; // 하나의 문자열로 합친 생년월일
            this.mail = mail;
        }
    }
}
