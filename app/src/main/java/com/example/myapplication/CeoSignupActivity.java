package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CeoSignupActivity extends AppCompatActivity {

    private boolean isPasswordConfirmed = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ceosignup);

        mAuth = FirebaseAuth.getInstance();

        // UI 요소 참조
        TextView backTextView = findViewById(R.id.back);
        EditText passwordEditText = findViewById(R.id.signPW);
        EditText confirmPasswordEditText = findViewById(R.id.signPW2);
        EditText NicknameEditText = findViewById(R.id.signceoNickName);
        EditText nameEditText = findViewById(R.id.signName);
        EditText idEditText = findViewById(R.id.signID);
        EditText ceoNumberEditText = findViewById(R.id.signceo1);
        Button confirmButton = findViewById(R.id.pwcheckbutton);
        Button signupButton = findViewById(R.id.signupbutton);
        EditText birthText = findViewById(R.id.signBirth);
        EditText birth2Text = findViewById(R.id.signBirth2);
        EditText birth3Text = findViewById(R.id.signBirth3);

        backTextView.setOnClickListener(v -> {
            Intent intent = new Intent(CeoSignupActivity.this, SelectionActivity.class);
            startActivity(intent);
        });

        confirmButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();

            if (password.equals(confirmPassword)) {
                Toast.makeText(CeoSignupActivity.this, "비밀번호가 일치합니다!", Toast.LENGTH_SHORT).show();
                isPasswordConfirmed = true;
            } else {
                Toast.makeText(CeoSignupActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                isPasswordConfirmed = false;
            }
        });

        signupButton.setOnClickListener(v -> {
            String email = idEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String name = nameEditText.getText().toString();
            String Nickname = NicknameEditText.getText().toString();
            String ceoNumber = ceoNumberEditText.getText().toString();
            String signBirth = birthText.getText().toString();
            String signBirth2 = birth2Text.getText().toString();
            String signBirth3 = birth3Text.getText().toString();

            // 생년월일을 하나의 문자열로 합치기
            String birth = signBirth + "-" + signBirth2 + "-" + signBirth3;


            if (name.isEmpty() || email.isEmpty() ||birth.isEmpty()|| password.isEmpty() || ceoNumber.isEmpty() || !isPasswordConfirmed) {
                Toast.makeText(CeoSignupActivity.this, "모든 필드를 채우고 비밀번호 확인을 해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid(); // Firebase Authentication의 UID를 가져옵니다.
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("CeoUsers");

                                CeoUser ceoUser = new CeoUser(name, Nickname, email, birth, password, ceoNumber);
                                databaseReference.child(uid).setValue(ceoUser) // UID를 키로 사용하여 데이터베이스에 CEO 사용자 정보 저장
                                        .addOnCompleteListener(taskDb -> {
                                            if (taskDb.isSuccessful()) {
                                                Toast.makeText(CeoSignupActivity.this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                                                new Handler().postDelayed(() -> {
                                                    Intent intent = new Intent(CeoSignupActivity.this, LoginActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }, 2500);
                                            } else {
                                                Toast.makeText(CeoSignupActivity.this, "데이터베이스 오류: " + taskDb.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(CeoSignupActivity.this, "Authentication 오류: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    public static class CeoUser {
        public String name, Nickname, email, birth, password, ceoNumber;

        public CeoUser(String name, String Nickname, String email, String birth, String password, String ceoNumber) {
            this.name = name;
            this.Nickname = Nickname;
            this.email = email;
            this.birth = birth;
            this.password = password;
            this.ceoNumber = ceoNumber;
        }
    }
}
