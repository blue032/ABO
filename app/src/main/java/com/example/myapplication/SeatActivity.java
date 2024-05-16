package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.TimeZone;

public class SeatActivity extends AppCompatActivity {
    private DatabaseReference tableStatusRef;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        // Firebase 참조 초기화
        tableStatusRef = FirebaseDatabase.getInstance().getReference("카페 테이블 상태");

        // 실시간 데이터베이스 감시 시작
        monitorTableStatus();

        // TextView 초기화
        statusTextView = findViewById(R.id.statusTextView);

        // 현재 시간 확인하여 영업 종료 텍스트 설정
        checkAndSetClosingStatus();

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
                    startActivity(new Intent(SeatActivity.this, NotificationActivity.class));
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

    private void monitorTableStatus() {
        tableStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TableStatus status = snapshot.getValue(TableStatus.class);
                    if (status != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTableUI(status.number, status.status);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("SeatActivity", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateTableUI(int tableNumber, boolean isOccupied) {
        // 현재 시간 확인하여 영업 종료 텍스트 설정
        checkAndSetClosingStatus();

        // 영업 종료 시간이 아닐 때만 테이블 상태를 업데이트
        if (!isClosingTime()) {
            int resId = getResources().getIdentifier("seat" + tableNumber, "id", getPackageName());
            ImageView seatView = findViewById(resId);
            if (seatView != null) {
                seatView.setImageResource(isOccupied ? R.drawable.seat_occupied : R.drawable.seat_empty);
            }
        }
    }

    private void checkAndSetClosingStatus() {
        if (isClosingTime()) {
            statusTextView.setText("영업 종료");
            statusTextView.setVisibility(View.VISIBLE);
        } else {
            statusTextView.setText("");
            statusTextView.setVisibility(View.GONE);
        }
    }

    private boolean isClosingTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 9 || hour >= 20; // 오전 9시 이전 또는 오후 8시 이후
    }

    private static class TableStatus {
        public int number;
        public boolean status;

        public TableStatus() {
            // Default constructor required for calls to DataSnapshot.getValue(TableStatus.class)
        }
    }
}