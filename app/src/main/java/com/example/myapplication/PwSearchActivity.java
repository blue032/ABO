package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PwSearchActivity extends AppCompatActivity {

    private EditText editmail, editName, editBirth, editPhone;
    private Button searchPwButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pw_search);

        // Firebase Database 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // EditText 인스턴스 초기화
        editmail = findViewById(R.id.editmail);
        editName = findViewById(R.id.editname);
        editBirth = findViewById(R.id.editbirth);
        editPhone = findViewById(R.id.editphone);

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

        editmail.addTextChangedListener(textWatcher);
        editName.addTextChangedListener(textWatcher);
        editBirth.addTextChangedListener(textWatcher);
        editPhone.addTextChangedListener(textWatcher);

        // 버튼 인스턴스 초기화 및 클릭 리스너 설정
        searchPwButton = findViewById(R.id.search_PW);
        searchPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findUserForPasswordReset();
            }
        });
    }

    private void checkInputs() {
        String mail = editmail.getText().toString();
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String phone = editPhone.getText().toString();

        // 모든 필드가 비어있지 않으면 "비밀번호 찾기" 버튼 활성화
        searchPwButton.setEnabled(!mail.isEmpty() && !name.isEmpty() && !birth.isEmpty() && !phone.isEmpty());
    }

    private void findUserForPasswordReset() {
        String mail = editmail.getText().toString();
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String phone = editPhone.getText().toString();

        // 문자열에서 연, 월, 일 추출
        String year = birth.substring(0, 4);
        String month = birth.substring(4, 6);
        String day = birth.substring(6, 8);

        // 추출한 연, 월, 일을 하나의 문자열로 합치기
        String birthDate = year + "-" + month + "-" + day;
        // Firebase에서 사용자 정보 조회
        databaseReference.orderByChild("mail").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getName().equals(name) && user.getBirth().equals(birthDate) && user.getPhone().equals(phone)) {
                        // 사용자 정보가 일치하는 경우, PwResetActivity로 정보를 전달
                        Intent intent = new Intent(PwSearchActivity.this, PwResetActivity.class);
                        intent.putExtra("userEmail", mail);
                        intent.putExtra("userName", name);
                        intent.putExtra("userBirth", birthDate);
                        intent.putExtra("userPhone", phone);
                        startActivity(intent);
                        return;
                    }
                }
                // 일치하는 사용자 정보가 없는 경우 처리
                Toast.makeText(PwSearchActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                Toast.makeText(PwSearchActivity.this, "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 사용자 클래스 (Firebase 데이터 구조에 맞게 정의)
    public static class User {
        private String name, birth, phone, mail;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public String getName() {
            return name;
        }

        public String getBirth() {
            return birth;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return mail;
        }
    }
}
