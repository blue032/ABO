package com.example.myapplication;
//다시

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class WaitingActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private Handler handler = new Handler();
    private Runnable runnable;
    private ArrayList<Orders> ordersList = new ArrayList<>();
    private EditText tv_waitingNumber;
    private EditText waitingTime;
    private int totalCount = 0;
    private long maxWaitingTimeMillis = 0;

    // 멤버 변수로 ScrollView와 TextView의 높이를 저장할 수 있습니다.
    private ScrollView scrollViewNumbers;
    private int textViewHeight = 0;

    private long estimatedWaitTimeMillis = -1; // 처음에는 -1로 설정
    private int previousTotalCount = -1; // 이전 totalCount 값

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // 현재 날짜의 요일을 Firebase에 추가
        updateDailyStatsInFirebase();

        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener(() -> {
            System.out.println("SwipeRefreshLayout Test");
            refreshLayout.setRefreshing(false);
        });


        // EditTexts
        tv_waitingNumber = findViewById(R.id.tv_waitingNumber);
        waitingTime = findViewById(R.id.waitingTime);

        // 파이어베이스 초기화
        setupFirebaseListener();

        // Runnable 초기화 및 시작
        setupRunnable();

        // ImageView 참조
        ImageView imageViewTimeChange = findViewById(R.id.imageViewCeoTimeChange);
        ImageView imageViewOrderChange = findViewById(R.id.imageViewCeoOrderChange);
        if (imageViewTimeChange != null) {
            imageViewTimeChange.setOnClickListener(v -> showTimeChangeDialog());
        } else {
            Log.e("WaitingActivity", "imageViewTimeChange is null");
        }

        if (imageViewOrderChange != null) {
            imageViewOrderChange.setOnClickListener(v -> Log.d("WaitingActivity", "Order Change Clicked"));
        } else {
            Log.e("WaitingActivity", "imageViewOrderChange is null");
        }

        // SharedPreferences에서 사장님 여부 확인
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isCeo = prefs.getBoolean("IsCeo", false);

        // 사장님일 경우에만 ImageView를 보이게 설정
        if (isCeo) {
            imageViewTimeChange.setVisibility(View.VISIBLE);
            imageViewOrderChange.setVisibility(View.VISIBLE);
        } else {
            imageViewTimeChange.setVisibility(View.GONE);
            imageViewOrderChange.setVisibility(View.GONE);
        }

        imageViewOrderChange.setOnClickListener(v -> {
            // Ceo_OrderChange 액티비티로 전환하기 위한 Intent 생성
            Intent intent = new Intent(WaitingActivity.this, Ceo_OrderChange.class);
            startActivity(intent); // 액티비티 시작
        });

        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
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
                startActivity(new Intent(WaitingActivity.this, NotificationActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                Intent intent = new Intent(WaitingActivity.this, MypageActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void setupFirebaseListener() {
        database = FirebaseDatabase.getInstance();
        String referencePath = "Order/2024/3/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        databaseReference = database.getReference(referencePath);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear(); // 리스트 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Orders order = snapshot.getValue(Orders.class);

                    if (order != null) {
                        ordersList.add(order); // 업데이트된 주문 리스트에 추가
                    }
                }
                updateWaitingTimeUI(); // 최대대기시간 업뎃
                checkOrdersTime();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UpdateWaitNumber", "Database error", databaseError.toException());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    private void setupRunnable(){
        runnable = new Runnable() {
            @Override
            public void run() {
                checkOrdersTime(); // 현재시간에 해당하는 대기번호 확인
                handler.postDelayed(this, 1000); // 1초마다 실행
            }
        };
        handler.post(runnable); // Runnable 실행
    }
    private PriorityQueue<Orders> ordersQueue = new PriorityQueue<>(new Comparator<Orders>() {
        @Override
        public int compare(Orders o1, Orders o2) {
            // 내림차순 정렬을 위한 비교
            return Long.compare(o2.getTotalWaitTimeMillis(), o1.getTotalWaitTimeMillis());
        }
    });

    public void updateDailyStatsInFirebase() {
        // 현재 날짜와 시간 구하기
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // 요일 계산하기
        Calendar calendar = Calendar.getInstance();
        String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String todayDay = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]; // Calendar.DAY_OF_WEEK는 1부터 시작(Sunday = 1)

        // Firebase 경로 설정
        DatabaseReference dailyStatsRef = FirebaseDatabase.getInstance().getReference("dailyStats/" + todayDate);

        // 요일과 시간 데이터 추가
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentDay", todayDay);
        updates.put("lastUpdatedTime", currentTime);

        dailyStatsRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Log.d("Firebase", "Daily stats updated successfully with day: " + todayDay + " and time: " + currentTime);
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to update daily stats", e);
        });
    }

    private void updateTotalCountInFirebase(int newCount) {
        // 변경될 totalCount 값과 함께 업데이트할 경로를 설정합니다.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        DatabaseReference countRef = database.getReference("dailyStats/" + todayDate + "/totalWaitCount");

        // setValue 메소드를 사용하여 데이터베이스에 새 대기번호를 저장합니다.
        countRef.setValue(newCount).addOnSuccessListener(aVoid -> {
            Log.d("WaitingActivity", "Total count updated successfully for " + todayDate + ": " + newCount);
        }).addOnFailureListener(e -> {
            Log.e("WaitingActivity", "Failed to update total count for " + todayDate, e);
        });
    }

    // 현재시간에 해당하는 대기번호 확인 후 화면에 표시하는 로직
    private void checkOrdersTime() {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        // 영업시간 설정
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 9);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        long startTimeMillis = startTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 20);
        endTime.set(Calendar.MINUTE, 59);
        endTime.set(Calendar.SECOND, 0);
        long endTimeMillis = endTime.getTimeInMillis();

        if (nowMillis < startTimeMillis || nowMillis > endTimeMillis) {
            saveWaitingInfo(0, 0); // 영업 종료 시 대기 번호와 대기 시간을 0으로 설정
            estimatedWaitTimeMillis = 0; // 추가: 예상 대기 시간도 초기화
            previousTotalCount = 0;
            return;
        }

        int currentCount = 0;
        long totalWaitTime = 0;

        // 주문 목록을 반복하여 현재 대기 번호 수를 계산
        for (Iterator<Orders> iterator = ordersList.iterator(); iterator.hasNext(); ) {
            Orders order = iterator.next();

            long orderTimeMillis = getOrderTimeMillis(order);
            // 메뉴 아이템의 총 대기 시간 계산
            long totalWaitTimeMillis = calculateTotalWaitTimeMillis(order);
            long orderEndTimeMillis = orderTimeMillis + totalWaitTimeMillis;

            // 주문이 현재 시간 이전에 들어왔다면
            if (orderTimeMillis <= nowMillis) {
                if (orderEndTimeMillis > nowMillis) {
                    currentCount++;
                    totalWaitTime += totalWaitTimeMillis;
                } else { // 주문 완료 시간이 현재 시간을 지났을 경우
                    iterator.remove();
                }
            }
        }

        totalCount = currentCount;

        // 예상 대기 시간 계산
        if (currentCount != previousTotalCount) { // totalCount가 변경된 경우에만 업데이트
            previousTotalCount = totalCount;
            updateWaitingTimeUI(); // 예상 대기 시간을 업데이트
        } else {
            waitingTime.setText(String.valueOf(estimatedWaitTimeMillis / (60 * 1000)));
            tv_waitingNumber.setText(String.valueOf(totalCount));
        }

        saveWaitingInfo(totalCount, estimatedWaitTimeMillis);
    }


    private long getOrderTimeMillis(Orders order) {
        Calendar orderCalendar = Calendar.getInstance();
        Orders.Time orderTime = order.getTime();
        orderCalendar.set(Calendar.HOUR_OF_DAY, orderTime.getHour());
        orderCalendar.set(Calendar.MINUTE, orderTime.getMinute());
        orderCalendar.set(Calendar.SECOND, orderTime.getSecond());
        return orderCalendar.getTimeInMillis();
    }

    // 주문 완료 시간 계산 메서드 수정
    private long calculateTotalWaitTimeMillis(Orders order) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);

        // 점심 시간대(12시 ~ 14시)에 메뉴 1개당 4분
        int minutesPerItem = (hour >= 12 && hour < 14) ? 4 : 2;

        return order.getMenu().stream()
                .mapToLong(item -> item.getQuantity() * minutesPerItem * 60 * 1000) // 각 메뉴 아이템의 대기 시간을 계산
                .sum();
    }

    private void updateCongestionStatusUI() {
        TextView textStatus = findViewById(R.id.textAvoidCongestion);
        TextView textVisitNow = findViewById(R.id.textVisitNow);
        TextView textVisitLater = findViewById(R.id.textVisitLater);
        LinearLayout timeInputLayout = findViewById(R.id.timeInputLayout);

        if (totalCount == 0 || estimatedWaitTimeMillis <= 7 * 60 * 1000) {
            // 여유 상태일 때의 텍스트 설정
            textStatus.setText("현재 여유 상태입니다");
            textVisitNow.setVisibility(View.VISIBLE);
            timeInputLayout.setVisibility(View.GONE);
            textVisitLater.setVisibility(View.GONE);
        } else {
            // 혼잡 또는 보통 상태일 때의 텍스트 설정
            if (estimatedWaitTimeMillis >= 20 * 60 * 1000) {
                textStatus.setText("현재 혼잡 상태입니다");
            } else {
                textStatus.setText("현재 보통 상태입니다");
            }

            Calendar now = Calendar.getInstance();
            long cumulativeWaitTimeMillis = 0;
            int count = 0;

            for (Orders order : ordersList) {
                if (count >= totalCount) break; // 화면에 표시된 대기 번호의 수만큼 계산
                cumulativeWaitTimeMillis += calculateTotalWaitTimeMillis(order);
                count++;
            }

            // 밀리초를 분 단위로 변환
            long cumulativeWaitTimeMinutes = cumulativeWaitTimeMillis / (60 * 1000);

            // 예상 방문 시간 계산
            Calendar estimatedVisitTime = (Calendar) now.clone();
            estimatedVisitTime.add(Calendar.MINUTE, (int) cumulativeWaitTimeMinutes);
            int hour = estimatedVisitTime.get(Calendar.HOUR_OF_DAY);
            int minute = estimatedVisitTime.get(Calendar.MINUTE);

            textVisitLater.setText(String.format("%02d시 %02d분에 방문하세요", hour, minute));

            textVisitNow.setVisibility(View.GONE);
            timeInputLayout.setVisibility(View.GONE);
            textVisitLater.setVisibility(View.VISIBLE);
        }
    }


    private void updateWaitingTimeUI() {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        estimatedWaitTimeMillis = prefs.getLong("EstimatedWaitTimeMillis", 0); // SharedPreferences에서 예상 대기 시간을 가져오기

        int estimatedWaitTimeMinutes = (int) (estimatedWaitTimeMillis / (60 * 1000));
        waitingTime.setText(String.valueOf(estimatedWaitTimeMinutes));
        tv_waitingNumber.setText(String.valueOf(totalCount));

        updateCongestionStatusUI(); // 혼잡 상태 업데이트

        saveWaitingInfo(totalCount, estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
    }




    // 랜덤 숫자를 생성하는 유틸리티 메서드
    private long getRandomNumber(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }


    public void addOrder(Orders newOrder) {
        // 새 주문을 주문 리스트에 추가
        ordersList.add(newOrder);
        long newOrderWaitTime = calculateTotalWaitTimeMillis(newOrder);
        newOrder.setTotalWaitTimeMillis(newOrderWaitTime);
        ordersQueue.add(newOrder);
        // 대기번호 증가
        totalCount++;
        // UI 업데이트
        updateWaitingTimeUI();
    }
    private void saveWaitingInfo(int waitingNumber, long estimatedWaitTimeMillis) {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("WaitingNumber", waitingNumber);
        editor.putLong("EstimatedWaitTimeMillis", estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
        editor.apply();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        handler.removeCallbacks(runnable);
    }

    // 팝업 다이얼로그를 표시하는 메소드
    private void showTimeChangeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_change); // 올바른 레이아웃 파일 이름을 확인하세요

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        final EditText editTime = dialog.findViewById(R.id.buttonSetTime); // 올바른 ID를 확인하세요
        Button buttonConfirm = dialog.findViewById(R.id.button_cancel); // 올바른 ID를 확인하세요

        buttonConfirm.setOnClickListener(v -> {
            // 시간 변경 로직
            String newTime = editTime.getText().toString();
            // 로직 구현...
            dialog.dismiss();
        });

        dialog.show();
    }
}