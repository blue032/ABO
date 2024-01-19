package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Signup1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup1);

        Button ceoButton = findViewById(R.id.btn_ceo);
        Button userButton = findViewById(R.id.btn_user);
        ImageView backButton = findViewById(R.id.back_arrow); // back_arrow 버튼 참조 추가

        // "사장님 전용" 버튼 클릭 시 ceosignupactivity로 이동
        ceoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup1Activity.this, CeoSignupActivity.class);
                startActivity(intent);
            }
        });

        // "사용자 전용" 버튼 클릭 시 usersignupactivity로 이동
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup1Activity.this, UserSignupActivity.class);
                startActivity(intent);
            }
        });

        // 뒤로가기(백) 버튼 클릭 시 LoginActivity로 이동
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup1Activity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
