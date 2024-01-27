package com.example.myapplication;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SeatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        // Intent에서 카페 이름 가져오기
        String cafeName = getIntent().getStringExtra("cafeName");

        // EditText에 카페 이름 설정
        EditText editTextCafeName = findViewById(R.id.editcafename);
        editTextCafeName.setText(cafeName);
    }
}
