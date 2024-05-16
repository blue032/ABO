package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {
    private String actionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        actionType = getIntent().getStringExtra("actionType"); // 인텐트에서 데이터 가져오기

        Button ceoButton = findViewById(R.id.btn_ceo);
        Button userButton = findViewById(R.id.btn_user);
        ImageView backButton = findViewById(R.id.backlogo); // back_arrow 버튼 참조 추가


        ceoButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, IdPwSelectionActivity.class);
            if ("signup".equals(actionType)) {
                // 회원가입 - 사장님 전용
                intent = new Intent(SelectionActivity.this, CeoSignupActivity.class);
            } else if ("findId".equals(actionType)) {
                // ID/PW 찾기 - 사장님 전용
                intent = new Intent(SelectionActivity.this, CeoIdSearchActivity.class);
                intent.putExtra("userType", "ceo");
            }
            else if ("findPw".equals(actionType)) {
                // ID/PW 찾기 - 사장님 전용
                intent = new Intent(SelectionActivity.this, CeoPwSearchActivity.class);
                intent.putExtra("userType", "ceo");
            }
            startActivity(intent);
        });


        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, IdPwSelectionActivity.class);
            if ("signup".equals(actionType)) {
                // 회원가입 - 사장님 전용
                intent = new Intent(SelectionActivity.this, UserSignupActivity.class);
            } else if ("findId".equals(actionType)) {
                // ID/PW 찾기 - 사장님 전용
                intent = new Intent(SelectionActivity.this, IdSearchActivity.class);
                intent.putExtra("userType", "ceo");
            }
            else if ("findPw".equals(actionType)) {
                // ID/PW 찾기 - 사장님 전용
                intent = new Intent(SelectionActivity.this, PwSearchActivity.class);
                intent.putExtra("userType", "ceo");
            }
            startActivity(intent);
        });


        // 뒤로가기(백) 버튼 클릭 시 LoginActivity로 이동
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectionActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
