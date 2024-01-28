package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IdKnownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_known);

        TextView idTextView = findViewById(R.id.IDknown);

        // 사용자 버전에서 받아온 이메일(ID)
        String userId = getIntent().getStringExtra("userEmail");

        // CEO 버전에서 받아온 이메일
        String ceoId = getIntent().getStringExtra("ceoemail");

        // 두 인텐트 중 하나가 null이 아닌 경우 해당 값을 표시
        String foundId = userId != null ? userId : ceoId;

        idTextView.setText(foundId);
    }
}
