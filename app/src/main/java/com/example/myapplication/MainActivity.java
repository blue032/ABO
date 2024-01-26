package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEmpty = findViewById(R.id.btn_empty);
        Button btnWaiting = findViewById(R.id.btn_waiting);
        Button btnMap = findViewById(R.id.mapcheckbutton);

        btnEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
                intent.putExtra("viewType", "emptySeats");
                startActivity(intent);
            }
        });

        btnWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
                intent.putExtra("viewType", "waitingList");
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        TextView tvCafeOO = findViewById(R.id.abo2);

        tvCafeOO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // O.O카페 TextView가 클릭되었을 때 수행할 동작
                Intent intent = new Intent(MainActivity.this, DetailpageActivity.class);
                // 필요한 경우 intent에 추가 데이터를 넣습니다.
                startActivity(intent);
            }
        });
    }
}
