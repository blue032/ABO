package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class CafelistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafelist);

        // 각 TextView에 대한 참조
        TextView cafeDream6 = findViewById(R.id.textView1);
        TextView snackBar = findViewById(R.id.textView2);
        TextView ooCafe = findViewById(R.id.textView3);
        TextView miyu = findViewById(R.id.textView4);
        TextView heum = findViewById(R.id.textView5);
        TextView cafeDream11 = findViewById(R.id.textView6);

        String viewType = getIntent().getStringExtra("viewType");

        // 각 카페 TextView에 대한 클릭 리스너 설정
        setOnClickListenerForCafe(cafeDream6, "카페드림(6호관)", viewType);
        setOnClickListenerForCafe(snackBar, "스낵바(13호관)", viewType);
        setOnClickListenerForCafe(ooCafe, "O.O카페(7호관)", viewType);
        setOnClickListenerForCafe(miyu, "미유(29호관)", viewType);
        setOnClickListenerForCafe(heum, "혜윰(기숙사)", viewType);
        setOnClickListenerForCafe(cafeDream11, "카페드림(11호관)", viewType);
    }

    private void setOnClickListenerForCafe(TextView cafeTextView, String cafeName, String viewType) {
        cafeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if ("emptySeats".equals(viewType)) {
                    intent = new Intent(CafelistActivity.this, SeatActivity.class);
                } else {
                    intent = new Intent(CafelistActivity.this, WaitingActivity.class);
                }
                intent.putExtra("cafeName", cafeName);
                startActivity(intent);
            }
        });
    }
}
