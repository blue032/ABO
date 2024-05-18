package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textViewSplash = findViewById(R.id.textViewSplash);
        String text = "CertaIN U";
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf("IN U");
        int end = start + "IN U".length();
        int customBlueColor = ContextCompat.getColor(this, R.color.custom_blue);
        spannableString.setSpan(new ForegroundColorSpan(customBlueColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewSplash.setText(spannableString);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 로그인 액티비티로 이동
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // 스플래시 액티비티 종료
                finish();
            }
        }, 2000); // 2초 후에 로그인 액티비티로 전환
    }
}
