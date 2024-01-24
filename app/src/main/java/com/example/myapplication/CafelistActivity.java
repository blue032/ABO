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

        TextView ooCafe = findViewById(R.id.textView3);

        String viewType = getIntent().getStringExtra("viewType");

        ooCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if ("emptySeats".equals(viewType)) {
                    intent = new Intent(CafelistActivity.this, SeatActivity.class);
                } else {
                    intent = new Intent(CafelistActivity.this, WaitingActivity.class);
                }
                startActivity(intent);
            }
        });
    }
}
