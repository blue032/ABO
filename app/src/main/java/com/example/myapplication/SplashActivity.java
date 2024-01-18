package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 메인 액티비티로 이동
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                // 스플래시 액티비티 종료
                finish();
            }
        }, 2000); // 2000ms = 2초 후에 메인 액티비티로 전환
    }
}
