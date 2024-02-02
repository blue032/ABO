package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class WaitingActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private final Handler handler = new Handler();
    private EditText editWaitingNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // Intent에서 카페 이름 가져오기
        String cafeName = getIntent().getStringExtra("cafeName");

        // EditText에 카페 이름 설정
        EditText editTextCafeName = findViewById(R.id.editcafename);
        editTextCafeName.setText(cafeName);

        // Firebase Database 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // EditText 참조
        editWaitingNumber = findViewById(R.id.editwaitingNumber);

        // 주기적으로 데이터 업데이트
        handler.post(runnable);

        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    Intent intent = new Intent(WaitingActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    Intent intent = new Intent(WaitingActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우
            }
        });
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadDataFromFirebase();
            handler.postDelayed(this, 1000); // 1분 후에 다시 실행
        }
    };

    private void loadDataFromFirebase() {
        // 현재 날짜와 시간 가져오기
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // 영업 시간 체크 (오전 9시 이전 또는 오후 8시 이후)
        if (hour < 9 || hour > 20) {
            editWaitingNumber.setText("영업종료");
            return;
        }

        // Firebase에서 현재 일시에 해당하는 대기번호 가져오기
        mDatabase.child("orders")
                .child("2024")
                .child("3")
                .child(String.valueOf(day))
                .orderByChild("time")
                .equalTo(hour + ":" + minute + ":" + second)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String waitingNumber = snapshot.child("waitnumber").getValue(String.class);
                                editWaitingNumber.setText(waitingNumber);
                                break; // 첫 번째 일치하는 항목만 사용
                            }
                        } else {
                            editWaitingNumber.setText("대기번호 없음");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // 오류 처리
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 액티비티가 파괴될 때 Runnable 중지
    }
}
