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

public class IdSearchActivity extends AppCompatActivity {

    private EditText editName, editBirth, editPhone;
    private Button searchIdButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_search);

        // Firebase Database 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // EditText 인스턴스 초기화
        editName = findViewById(R.id.editname);
        editBirth = findViewById(R.id.editbirth);
        editPhone = findViewById(R.id.editmail);

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

        editName.addTextChangedListener(textWatcher);
        editBirth.addTextChangedListener(textWatcher);
        editPhone.addTextChangedListener(textWatcher);

        // 버튼 인스턴스 초기화 및 클릭 리스너 설정
        searchIdButton = findViewById(R.id.search_ID);
        searchIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findUserId();
            }
        });
    }

    private void checkInputs() {
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String phone = editPhone.getText().toString();

        searchIdButton.setEnabled(!name.isEmpty() && !birth.isEmpty() && !phone.isEmpty());
    }

    private void findUserId() {
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
        databaseReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getBirth().equals(birthDate) && user.getPhone().equals(phone)) {
                        // 사용자 찾음, IdKnownActivity로 이메일 전달
                        Intent intent = new Intent(IdSearchActivity.this, IdKnownActivity.class);
                        intent.putExtra("userEmail", user.getMail()); // 사용자의 이메일을 전달
                        startActivity(intent);
                        userFound = true;
                        break;
                    }
                }
                if (!userFound) {
                    // 일치하는 사용자가 없을 경우 처리
                    Toast.makeText(IdSearchActivity.this, "일치하는 사용자가 없습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                Toast.makeText(IdSearchActivity.this, "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
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

        public String getMail() {
            return mail;
        }
    }
}
