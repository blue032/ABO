package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IdKnownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_known);

        // ID를 표시할 TextView 참조
        TextView idTextView = findViewById(R.id.IDknown);

        // 인텐트에서 찾은 이메일 받아오기
        String foundId = getIntent().getStringExtra("userEmail");

        // TextView에 ID 설정
        idTextView.setText(foundId);
    }
}

