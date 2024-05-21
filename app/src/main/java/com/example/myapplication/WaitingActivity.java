package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class WaitingActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private ArrayList<Orders> ordersList = new ArrayList<>();
    private int totalCount = 0;
    private long maxWaitingTimeMillis = 0;

    private long estimatedWaitTimeMillis = -1; // 처음에는 -1로 설정
    private int previousTotalCount = -1; // 이전 totalCount 값
    private Handler changeHandler = new Handler(); // 추가된 핸들러
    private Runnable resetRunnable;
    private TextView tv_waitingNumber;
    private TextView waitingTime;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // 현재 날짜의 요일을 Firebase에 추가
        updateDailyStatsInFirebase();

        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener(() -> {
            Log.d("WaitingActivity", "SwipeRefreshLayout Test");
            refreshLayout.setRefreshing(false);
        });

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WaitingActivity.this, CafeDetailPageActivity.class);
                startActivity(intent);
                finish();  // 현재 액티비티 종료
            }
        });

        // 파이어베이스 초기화
        setupFirebaseListener();

        // Runnable 초기화 및 시작
        setupRunnable();

        // ImageView 및 TextView 참조
        ImageView imageViewTimeChange = findViewById(R.id.imageViewCeoTimeChange);
        ImageView imageViewOrderChange = findViewById(R.id.imageViewCeoOrderChange);
        TextView timechange = findViewById(R.id.timechange);
        TextView orderchange = findViewById(R.id.orderchange);
        tv_waitingNumber = findViewById(R.id.tv_waitingNumber);
        waitingTime = findViewById(R.id.waitingTime);

        // 밑줄 추가
        underlineTextView(timechange);
        underlineTextView(orderchange);

        // 클릭 리스너 설정
        timechange.setOnClickListener(v -> {
            Log.d("WaitingActivity", "Time Change Clicked");
            showTimeChangeDialog();
        });

        orderchange.setOnClickListener(v -> {
            Log.d("WaitingActivity", "Order Change Clicked");
            Intent intent = new Intent(WaitingActivity.this, Ceo_OrderChange.class);
            startActivity(intent);
        });

        // SharedPreferences에서 사장님 여부 확인
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isCeo = prefs.getBoolean("IsCeo", false);

        // 사장님일 경우에만 ImageView와 TextView를 보이게 설정
        if (isCeo) {
            imageViewTimeChange.setVisibility(View.VISIBLE);
            imageViewOrderChange.setVisibility(View.VISIBLE);
            timechange.setVisibility(View.VISIBLE);
            orderchange.setVisibility(View.VISIBLE);
        } else {
            imageViewTimeChange.setVisibility(View.GONE);
            imageViewOrderChange.setVisibility(View.GONE);
            timechange.setVisibility(View.GONE);
            orderchange.setVisibility(View.GONE);
        }

    }

    private void underlineTextView(TextView textView) {
        SpannableString content = new SpannableString(textView.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(content);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("WaitingActivity", "Activity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("WaitingActivity", "Activity paused");
    }

    private void showTimeChangeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_time_change, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView buttonSetTime = dialogView.findViewById(R.id.buttonSetTime);
        TextView buttonCancel = dialogView.findViewById(R.id.button_cancel);

        buttonSetTime.setOnClickListener(v -> {
            EditText editMinute = dialogView.findViewById(R.id.editminute);
            String minuteText = editMinute.getText().toString();
            if (!minuteText.isEmpty()) {
                int minutes = Integer.parseInt(minuteText);
                setTemporaryWaitTime(minutes);
            }
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setTemporaryWaitTime(int minutes) {
        // 일시적인 대기 시간을 설정하고 화면에 표시
        long temporaryWaitTimeMillis = minutes * 60 * 1000;
        waitingTime.setText(String.valueOf(minutes));
        estimatedWaitTimeMillis = temporaryWaitTimeMillis;
        tv_waitingNumber.setText(String.valueOf(totalCount));

        // 3분 후 원래 대기 시간 계산 방식으로 돌아가기 위한 Runnable 설정
        resetRunnable = () -> {
            setupFirebaseListener(); // Firebase 리스너 재설정
            updateWaitingTimeUI(); // 대기 시간 UI 업데이트
        };

        // 3분(180000밀리초) 후에 Runnable 실행
        changeHandler.postDelayed(resetRunnable, 180000);
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
                updateWaitingTimeUI(); // 최대 대기 시간 업데이트
                checkOrdersTime();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UpdateWaitNumber", "Database error", databaseError.toException());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                checkOrdersTime(); // 현재 시간에 해당하는 대기 번호 확인
                handler.postDelayed(this, 1000); // 1초마다 실행
            }
        };
        handler.post(runnable); // Runnable 실행
    }

    public void updateDailyStatsInFirebase() {
        // 현재 날짜와 시간 구하기
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // 요일 계산하기
        Calendar calendar = Calendar.getInstance();
        String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String todayDay = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]; // Calendar.DAY_OF_WEEK는 1부터 시작 (Sunday = 1)

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

    // 현재 시간에 해당하는 대기 번호 확인 후 화면에 표시하는 로직
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
        for (Orders order : ordersList) {
            long orderTimeMillis = getOrderTimeMillis(order);
            // 메뉴 아이템의 총 대기 시간 계산
            long totalWaitTimeMillis = calculateTotalWaitTimeMillis(order);
            long orderEndTimeMillis = orderTimeMillis + totalWaitTimeMillis;

            // 주문이 현재 시간 이전에 들어왔다면
            if (orderTimeMillis <= nowMillis) {
                if (orderEndTimeMillis > nowMillis) {
                    currentCount++;
                    totalWaitTime += totalWaitTimeMillis;
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

        // 점심 시간대 (12시 ~ 14시)에 메뉴 1개당 4분
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

        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        long recommendedVisitTimeMillis = prefs.getLong("RecommendedVisitTimeMillis", 0);

        if (recommendedVisitTimeMillis > 0) {
            // 방문 추천 시간을 Calendar 객체로 변환
            Calendar recommendedVisitTime = Calendar.getInstance();
            recommendedVisitTime.setTimeInMillis(recommendedVisitTimeMillis);
            int hour = recommendedVisitTime.get(Calendar.HOUR_OF_DAY);
            int minute = recommendedVisitTime.get(Calendar.MINUTE);

            // TextView에 예상 방문 시간을 설정
            textVisitLater.setText(String.format("%02d시 %02d분에 방문하세요", hour, minute));
        }

        if (totalCount == 0 || estimatedWaitTimeMillis <= 7 * 60 * 1000) {
            textStatus.setText("현재 여유 상태입니다");
            textVisitNow.setVisibility(View.VISIBLE);
            textVisitLater.setVisibility(View.GONE);
        } else {
            if (estimatedWaitTimeMillis >= 20 * 60 * 1000) {
                textStatus.setText("현재 혼잡 상태를 피하려면");
            } else {
                textStatus.setText("현재 보통 상태를 피하려면");
            }
            textVisitNow.setVisibility(View.GONE);
            textVisitLater.setVisibility(View.VISIBLE);
        }
    }


    private void updateCircularProgress() {
        ImageView circularProgress = findViewById(R.id.circularProgress);

        if (estimatedWaitTimeMillis <= 7 * 60 * 1000) { // 7분 이하
            circularProgress.setImageResource(R.drawable.graph_30);
        } else if (estimatedWaitTimeMillis >= 20 * 60 * 1000) { // 20분 이상
            circularProgress.setImageResource(R.drawable.graph_90);
        } else { // 나머지
            circularProgress.setImageResource(R.drawable.graph_60);
        }
    }

    private void updateWaitingTimeUI() {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        estimatedWaitTimeMillis = prefs.getLong("EstimatedWaitTimeMillis", 0); // SharedPreferences에서 예상 대기 시간을 가져오기

        int estimatedWaitTimeMinutes = (int) (estimatedWaitTimeMillis / (60 * 1000));
        waitingTime.setText(String.valueOf(estimatedWaitTimeMinutes));
        tv_waitingNumber.setText(String.valueOf(totalCount));

        updateCircularProgress(); // circularProgress 업데이트
        updateCongestionStatusUI(); // 혼잡 상태 업데이트

        saveWaitingInfo(totalCount, estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
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
        changeHandler.removeCallbacks(resetRunnable); // 추가된 부분: 핸들러 콜백 제거
        Log.d("WaitingActivity", "Activity destroyed");
    }
}
