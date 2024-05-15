package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView textView = findViewById(R.id.textViewSplash);
        String text = "certaIN U";
        SpannableString spannableString = new SpannableString(text);

        // 사용자 정의 색상 적용
        int colorBlack = ContextCompat.getColor(this, R.color.black);  // 커스텀 블랙
        int colorLightBlue = ContextCompat.getColor(this, R.color.lightBlue);  // 커스텀 라이트블루

        // "certa" 부분을 검정색으로 설정
        spannableString.setSpan(new ForegroundColorSpan(colorBlack), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // "IN U" 부분을 라이트블루로 설정
        spannableString.setSpan(new ForegroundColorSpan(colorLightBlue), 5, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // TextView에 SpannableString 적용
        textView.setText(spannableString);

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
