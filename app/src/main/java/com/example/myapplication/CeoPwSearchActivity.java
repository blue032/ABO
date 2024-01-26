package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CeoPwSearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pw_search);

        // 버튼 인스턴스를 찾아서 초기화합니다.
        Button searchIdButton = findViewById(R.id.search_PW);

        // 버튼에 클릭 리스너를 설정합니다.
        searchIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PwSearchActivity로 인텐트를 생성하고 시작합니다.
                Intent intent = new Intent(CeoPwSearchActivity.this, PwResetActivity.class);
                startActivity(intent);
            }
        });
    }
}
