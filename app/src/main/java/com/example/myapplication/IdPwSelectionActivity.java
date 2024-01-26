package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class IdPwSelectionActivity extends AppCompatActivity {
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idpw_selection);

        userType = getIntent().getStringExtra("userType"); // 인텐트에서 사용자 유형 가져오기

        Button idButton = findViewById(R.id.button_id); // ID 찾기 버튼
        Button pwButton = findViewById(R.id.button_pw); // PW 찾기 버튼

        idButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("ceo".equals(userType)) {
                    // 사장님 전용 ID 찾기 액티비티로 이동
                    startActivity(new Intent(IdPwSelectionActivity.this, CeoIdSearchActivity.class));
                } else {
                    // 사용자 전용 ID 찾기 액티비티로 이동
                    startActivity(new Intent(IdPwSelectionActivity.this, IdSearchActivity.class));
                }
            }
        });

        pwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("ceo".equals(userType)) {
                    // 사장님 전용 ID 찾기 액티비티로 이동
                    startActivity(new Intent(IdPwSelectionActivity.this, CeoPwSearchActivity.class));
                } else {
                    // 사용자 전용 ID 찾기 액티비티로 이동
                    startActivity(new Intent(IdPwSelectionActivity.this, PwSearchActivity.class));
                }
            }
        });
    }
}
