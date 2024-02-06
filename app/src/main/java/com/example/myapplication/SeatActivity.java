package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        Button bluetoothButton = findViewById(R.id.button_bluetooth);

        // 버튼에 클릭 리스너를 설정합니다.
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BluetoothDeviceActivity로 전환하는 인텐트를 생성합니다.
                Intent intent = new Intent(SeatActivity.this, BluetoothDeviceActivity.class);

                // 인텐트를 시작합니다.
                startActivity(intent);
            }
        });
        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(SeatActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(SeatActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(SeatActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });

    }
}


