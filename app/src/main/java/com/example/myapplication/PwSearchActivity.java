package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    String userId = snapshot.getKey();

                    if (user != null && user.getName().equals(name) && user.getBirth().equals(birthDate) && user.getPhone().equals(phone)) {
                        Intent intent = new Intent(PwSearchActivity.this, PwResetActivity.class);
                        intent.putExtra("userEmail", mail);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                        return;
                    }
                }
                Toast.makeText(PwSearchActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PwSearchActivity.this, "오류가 발생했습니다.", Toast.LENGTH_LONG).show();
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
