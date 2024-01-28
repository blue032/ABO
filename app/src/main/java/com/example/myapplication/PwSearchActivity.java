package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PwSearchActivity extends AppCompatActivity {

    private EditText editmail, editName, editBirth, editPhone;
    private Button searchPwButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth; // Firebase 인증 객체 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pw_search);

        mAuth = FirebaseAuth.getInstance(); // Firebase 인증 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        editmail = findViewById(R.id.editmail);
        editName = findViewById(R.id.editname);
        editBirth = findViewById(R.id.editbirth);
        editPhone = findViewById(R.id.editphone);

        searchPwButton = findViewById(R.id.search_PW);
        searchPwButton.setOnClickListener(v -> findUserForPasswordReset());
    }

    private void findUserForPasswordReset() {
        String mail = editmail.getText().toString();
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String phone = editPhone.getText().toString();

        String year = birth.substring(0, 4);
        String month = birth.substring(4, 6);
        String day = birth.substring(6, 8);

        String birthDate = year + "-" + month + "-" + day;

        databaseReference.orderByChild("mail").equalTo(mail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (user != null && user.getName().equals(name) && user.getBirth().equals(birthDate) && user.getPhone().equals(phone)) {
                        userFound = true;
                        sendPasswordResetEmail(mail);
                        break;
                    }
                }
                if (!userFound) {
                    Toast.makeText(PwSearchActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PwSearchActivity.this, "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PwSearchActivity.this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PwSearchActivity.this, "이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User 클래스가 필요합니다. 이 클래스는 Firebase Realtime Database의 'Users' 노드 구조에 맞춰져 있어야 합니다.
    public static class User {
        private String name, birth, phone, mail;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        // getter 및 setter 메소드
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
