package com.example.myapplication;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SeatActivity extends AppCompatActivity {
    private DatabaseReference tableStatusRef;
    private TextView statusTextView;
    private ImageView reloadIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        ImageView back = (ImageView)findViewById(R.id.back);


        // "back" 텍스트뷰 클릭 이벤트
        back.setOnClickListener(v -> {
            Intent intent = new Intent(SeatActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Firebase 참조 초기화
        tableStatusRef = FirebaseDatabase.getInstance().getReference("카페 테이블 상태");

        // 실시간 데이터베이스 감시 시작
        monitorTableStatus();

        // TextView 초기화
        statusTextView = findViewById(R.id.statusTextView);
        reloadIcon = findViewById(R.id.reload_icon);

        // 현재 시간 확인하여 영업 종료 텍스트 설정
        checkAndSetClosingStatus();

        // Reload 버튼 클릭 이벤트 설정
        reloadIcon.setOnClickListener(v -> {
            // 애니메이션 시작
            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(reloadIcon, "rotation", 0f, 360f);
            rotateAnimator.setDuration(1000); // 1초 동안 애니메이션 실행

            // 애니메이션 리스너 설정
            rotateAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // 애니메이션 시작 시 동작
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // 애니메이션 종료 시 동작
                    monitorTableStatus();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // 애니메이션 취소 시 동작
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // 애니메이션 반복 시 동작
                }
            });

            rotateAnimator.start();
        });
    }

    private void monitorTableStatus() {
        tableStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // 데이터를 가져온 후 timestamp 필드를 무시하고 TableStatus 객체를 생성합니다.
                    int tableNumber = snapshot.child("number").getValue(Integer.class);
                    boolean status = snapshot.child("status").getValue(Boolean.class);
                    // timestamp는 가져오지 않음

                    TableStatus tableStatus = new TableStatus(tableNumber, status);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTableUI(tableStatus.number, tableStatus.status);
                        }
                    });
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
                seatView.setImageResource(isOccupied ? R.drawable.fullseat : R.drawable.emptyseat);
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

        if (isOpeningTime()) {
            resetAllTables();
        }
    }

    private boolean isClosingTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 9 || hour >= 20; // 오전 9시 이전 또는 오후 8시 이후
    }

    private boolean isOpeningTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour == 9 && minute == 0; // 오전 9시 정각
    }

    private void resetAllTables() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestampString = sdf.format(calendar.getTime()); // timestamp를 문자열로 변환

        for (int i = 1; i <= 10; i++) { // Assuming there are 10 tables
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            ImageView seatView = findViewById(resId);
            if (seatView != null) {
                seatView.setImageResource(R.drawable.emptyseat);
            }

            // Update the database
            TableStatus tableStatus = new TableStatus(i, false);
            tableStatusRef.child("table" + i).setValue(tableStatus);
        }
    }

    private static class TableStatus {
        public int number;
        public boolean status;

        public TableStatus() {
            // Default constructor required for calls to DataSnapshot.getValue(TableStatus.class)
        }

        public TableStatus(int number, boolean status) {
            this.number = number;
            this.status = status;
        }
    }
}