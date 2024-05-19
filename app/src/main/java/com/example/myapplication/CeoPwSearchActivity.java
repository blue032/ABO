package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class CeoPwSearchActivity extends AppCompatActivity {

    private EditText editEmail, editName, editBirth, editCeoNumber;
    private Button searchPwButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ceo_pw_search);

        mAuth = FirebaseAuth.getInstance();

        ImageView back = (ImageView)findViewById(R.id.back);
        editEmail = findViewById(R.id.editID);
        editName = findViewById(R.id.editname);
        editBirth = findViewById(R.id.editbirth);
        editCeoNumber = findViewById(R.id.editmail);
        searchPwButton = findViewById(R.id.search_PW);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(CeoPwSearchActivity.this, SelectionActivity.class);
            startActivity(intent);
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkFieldsForEmptyValues();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        // 모든 EditText에 TextWatcher 추가
        editEmail.addTextChangedListener(textWatcher);
        editName.addTextChangedListener(textWatcher);
        editBirth.addTextChangedListener(textWatcher);
        editCeoNumber.addTextChangedListener(textWatcher);

        // 초기 상태에서는 버튼 비활성화
        checkFieldsForEmptyValues();

        searchPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPasswordResetEmail();
            }
        });
    }

    private void checkFieldsForEmptyValues() {
        String email = editEmail.getText().toString();
        String name = editName.getText().toString();
        String birth = editBirth.getText().toString();
        String ceoNumber = editCeoNumber.getText().toString();

        // 모든 필드가 비어있지 않은 경우에만 버튼 활성화
        searchPwButton.setEnabled(!email.isEmpty() && !name.isEmpty() && !birth.isEmpty() && !ceoNumber.isEmpty());
    }

    private void sendPasswordResetEmail() {
        String email = editEmail.getText().toString().trim();

        // Firebase로 비밀번호 재설정 이메일 보내기
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CeoPwSearchActivity.this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CeoPwSearchActivity.this, "비밀번호 재설정 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
