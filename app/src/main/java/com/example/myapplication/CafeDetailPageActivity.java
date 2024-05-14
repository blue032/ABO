package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class CafeDetailPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafedetailpage);

        ImageView backLogo = (ImageView) findViewById(R.id.backlogo);
        backLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CafeDetailPageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // 현재 액티비티 종료
            }
        });


        MaterialButton buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CafeDetailPageActivity.this, SeatActivity.class);
                startActivity(intent);
            }
        });

        MaterialButton buttonWait = findViewById(R.id.buttonWait);
        buttonWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CafeDetailPageActivity.this, WaitingActivity.class);
                startActivity(intent);
            }
        });

        TextView seeMore = findViewById(R.id.seemore);
        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CafeDetailPageActivity.this, CafeMenuActivity.class);
                startActivity(intent);
            }
        });
    }
}
