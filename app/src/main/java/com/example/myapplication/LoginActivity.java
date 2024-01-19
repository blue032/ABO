package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 회원가입 버튼을 찾습니다
        Button signInButton = findViewById(R.id.signin);

        // 버튼에 클릭 리스너를 설정합니다
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인텐트를 생성하여 Signup1 액티비티를 시작합니다
                Intent intent = new Intent(LoginActivity.this, Signup1Activity.class);
                startActivity(intent);
            }
        });
    }
}
