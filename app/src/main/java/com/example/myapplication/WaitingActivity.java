package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import java.util.Locale;

public class WaitingActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener; //데이터 변경있을 때
    private Handler handler = new Handler();
    private Runnable runnable;
    private ArrayList<Orders> ordersList = new ArrayList<>();
    private TextView tv_waitingNumber;
    private TextView textViewSuffix; //~번입니다
    private int totalCount = 0; //전체 대기 수
    private EditText etEstimatedTimeInput;

    // 멤버 변수로 ScrollView와 TextView의 높이를 저장할 수 있습니다.
    private ScrollView scrollViewNumbers;
    private int textViewHeight = 0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("SwipeRefreshLayout Test");
                refreshLayout.setRefreshing(false);
            }
        });

        // Intent에서 카페 이름 가져오기
        String cafeName = getIntent().getStringExtra("cafeName");

        // EditText에 카페 이름 설정
        EditText editTextCafeName = findViewById(R.id.editcafename);
        editTextCafeName.setText(cafeName);

        // TextView
        tv_waitingNumber = findViewById(R.id.tv_waitingNumber);
        textViewSuffix = findViewById(R.id.textView);


        //파이어베이스 초기화
        setupFirebaseListener();

        //Runnable 초기화 및 시작
        setupRunnable();

        // ImageView 참조
        ImageView imageViewTimeChange = findViewById(R.id.imageViewCeoTimeChange);
        ImageView imageViewOrderChange = findViewById(R.id.imageViewCeoOrderChange);

        //사장님이 최대대기시간 변경할 수 있도록
        imageViewTimeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeChangeDialog();
            }
        });

        //사장님이 주문 내역을 변경할 수 있도록
        /*
        imageViewOrderChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeChangeDialog();
            }
        });
        */
/*
        // '-' 버튼 클릭 리스너 설정
        findViewById(R.id.button_decrease).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPickerDialog(false);
            }
        });

        // '+' 버튼 클릭 리스너 설정
        findViewById(R.id.button_increase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPickerDialog(true);
            }
        });

 */

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
                    startActivity(new Intent(WaitingActivity.this, NotificationActivity.class));
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


    private void setupFirebaseListener() {
        database = FirebaseDatabase.getInstance();
        String referencePath = "Orders/2024/3/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
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
                checkOrdersTime(); //현재시간에 해당하는 대기번호 확인
                handler.postDelayed(this, 1000); //1초마다 실행
            }
        };
        handler.post(runnable); //Runnable 실행
    }

    //현재시간에 해당하는 대기번호 확인 후 화면에 표시하는 로직
    private void checkOrdersTime() {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        //영업시간 설정
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 9);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        long startTimeMillis = startTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 20);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);
        long endTimeMillis = endTime.getTimeInMillis();

        if (nowMillis < startTimeMillis || nowMillis > endTimeMillis){
            updateUIWithClosed();
            return;
        }
        int currentCount = 0;

        // 주문 목록을 반복하여 현재 대기 번호 수를 계산
        for (Orders order : ordersList) {
            Calendar orderCalendar = Calendar.getInstance();
            Orders.Time orderTime = order.getTime();
            orderCalendar.set(Calendar.HOUR_OF_DAY, orderTime.getHour());
            orderCalendar.set(Calendar.MINUTE, orderTime.getMinute());
            orderCalendar.set(Calendar.SECOND, orderTime.getSecond());
            long orderTimeMillis = orderCalendar.getTimeInMillis();

            // 메뉴 아이템의 총 대기 시간 계산
            long totalWaitTimeMillis = order.getMenu().stream()
                    .mapToLong(item -> item.getQuantity() * 2 * 60 * 1000)
                    .sum();

            long orderEndTimeMillis = orderTimeMillis + totalWaitTimeMillis;

            // 주문이 현재 시간 이전에 들어왔다면
            if (orderTimeMillis <= nowMillis) {
                // 주문 완료 시간이 현재 시간을 지나지 않았으면 대기번호 증가
                if (orderEndTimeMillis > nowMillis) {
                    currentCount++;
                    // 예상 완료 시간을 포맷하여 표시
                    updateEstimatedCompletionTime(orderEndTimeMillis);
                } else {
                    // 주문의 완료 시간이 현재 시간을 지났고 currentCount가 0보다 크면 대기번호 감소
                    if (currentCount > 0) {
                        currentCount--;
                    }
                }
            }
        }
        totalCount = currentCount; // 현재 계산된 대기 수를 totalCount에 반영
        updateUIWithCurrentCount(); // UI 업데이트

    }

    private void updateUIWithClosed() {
        String closeText = "영업종료";
        tv_waitingNumber.setText(closeText);
        textViewSuffix.setVisibility(View.GONE);
        saveTotalCount(0); // 영업 종료 시 혼잡도 상태를 녹색으로
    }

    public void addOrder(Orders newOrder){
        // 새 주문을 주문 리스트에 추가
        ordersList.add(newOrder);
        // 대기번호 증가
        totalCount++;
        // UI 업데이트
        updateUIWithCurrentCount();
    }
    private void updateEstimatedCompletionTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = dateFormat.format(calendar.getTime());

    }



    private void updateUIWithCurrentCount() {
        tv_waitingNumber.setText(Integer.toString(totalCount));
        textViewSuffix.setVisibility(View.VISIBLE);
        saveTotalCount(totalCount); // 혼잡도 상태 저장

    }

    private void saveTotalCount(int totalCount){
        String status;
        if (totalCount <= 2){
            status = "icon_green";
        }else if (totalCount > 2 && totalCount <= 6){
            status = "icon_blue";
        }else {
            status = "icon_red";
        }
        SharedPreferences sharedPreferences = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CongestionStatus", "icon_green");
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
        // 커스텀 레이아웃을 사용하여 다이얼로그 생성
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_change);

        // 다이얼로그 크기 설정
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // EditText, Button 참조
        EditText editTime = dialog.findViewById(R.id.edit_time);
        Button buttonConfirm = dialog.findViewById(R.id.button_confirm);
        Button buttonDecrease = dialog.findViewById(R.id.button_decrease);
        Button buttonIncrease = dialog.findViewById(R.id.button_increase);
        Button buttonCancel = dialog.findViewById(R.id.button_cancel); // 취소 버튼 참조

        // 확인 버튼 리스너 설정
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 시간 변경 로직
                // 예시: String newTime = editTime.getText().toString();
                dialog.dismiss();
            }
        });

        // 증가, 감소 버튼 리스너 설정
        buttonDecrease.setOnClickListener(v -> {
            // 시간 감소 로직
        });

        buttonIncrease.setOnClickListener(v -> {
            // 시간 증가 로직
        });
        // 취소 버튼 리스너 설정
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });

        dialog.show();
    }
/*
    private void showNumberPickerDialog(boolean isIncrease) {
        final Dialog d = new Dialog(WaitingActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_picker_dialog);

        Button btnSet = (Button) d.findViewById(R.id.button1);
        Button btnCancel = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.number_picker);

        np.setMaxValue(18); // 숫자 90에 해당
        np.setMinValue(1);  // 숫자 5에 해당
        String[] nums = new String[18];
        for(int i=0; i<nums.length; i++)
            nums[i] = Integer.toString((i+1)*5);

        np.setDisplayedValues(nums);
        np.setWrapSelectorWheel(false);

        if (isIncrease) {
            np.setValue(18); // '+' 버튼을 눌렀을 때
        } else {
            np.setValue(1); // '-' 버튼을 눌렀을 때
        }

        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 값을 설정하고 다이얼로그를 닫습니다.
                // 예를 들어, 값을 EditText에 설정할 수 있습니다.
                d.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // 다이얼로그를 닫습니다.
            }
        });

        d.show();
    }
*/

}
