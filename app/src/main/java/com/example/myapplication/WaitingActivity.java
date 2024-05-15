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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private long maxWaitingTimeMillis = 0; //최대대기시간을 밀리초로 저장


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // 현재 날짜의 요일을 Firebase에 추가
        updateDailyStatsInFirebase();

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
        if (imageViewTimeChange != null) {
            imageViewTimeChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 시간 변경 다이얼로그 표시
                    showTimeChangeDialog();
                }
            });
        } else {
            Log.e("WaitingActivity", "imageViewTimeChange is null");
        }

        if (imageViewOrderChange != null) {
            imageViewOrderChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 주문 변경 로직 처리
                    Log.d("WaitingActivity", "Order Change Clicked");
                }
            });
        } else {
            Log.e("WaitingActivity", "imageViewOrderChange is null");
        }
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

        imageViewOrderChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ceo_OrderChange 액티비티로 전환하기 위한 Intent 생성
                Intent intent = new Intent(WaitingActivity.this, Ceo_OrderChange.class);
                startActivity(intent); // 액티비티 시작
            }
        });

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
                updateWaitingTimeUI(); //최대대기시간 업뎃
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
            // 서버로 데이터 전송하는 기능 호출
            //sendDataToServer(todayDate, currentTime, todayDay);
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to update daily stats", e);
        });
    }

   /* private void sendDataToServer(String date, String currentTime, String currentDay) {
        DatabaseReference dailyStatsRef = FirebaseDatabase.getInstance().getReference("dailyStats/" + date);
        dailyStatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentWaitCount = snapshot.child("currentWaitCount").getValue(Integer.class);
                if (currentWaitCount != null) {
                    OkHttpClient client = new OkHttpClient();
                    MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("date", date);
                        postData.put("time", currentTime);
                        postData.put("dayOfWeek", currentDay);
                        postData.put("waitingNumber", currentWaitCount);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, postData.toString());
                    Request request = new Request.Builder()
                            .url("http://yourserver.com/data")
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("HttpError", "Failed to send data to server.", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                Log.e("HttpError", "Server response was not successful.");
                            } else {
                                Log.i("HttpSuccess", "Data sent successfully");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read value.", error.toException());
            }
        });
    }*/


    private void updateTotalCountInFirebase(int newCount) {
        // 변경될 totalCount 값과 함께 업데이트할 경로를 설정합니다.
        // "dailyStats/[today's date]/totalWaitCount" 에 저장하고자 할 때
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        DatabaseReference countRef = database.getReference("dailyStats/" + todayDate + "/totalWaitCount");

        // setValue 메소드를 사용하여 데이터베이스에 새 대기번호를 저장합니다.
        countRef.setValue(newCount).addOnSuccessListener(aVoid -> {
            // 데이터베이스 업데이트에 성공했을 때 할 작업 (예: Toast 메시지 표시)
            Log.d("WaitingActivity", "Total count updated successfully for " + todayDate + ": " + newCount);
        }).addOnFailureListener(e -> {
            // 데이터베이스 업데이트에 실패했을 때 할 작업
            Log.e("WaitingActivity", "Failed to update total count for " + todayDate, e);
        });
    }

    //현재시간에 해당하는 대기번호 확인 후 화면에 표시하는 로직
    private void checkOrdersTime() {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        //영업시간 설정
        /*Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 1);
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
        }*/
        int currentCount = 0;
        maxWaitingTimeMillis = 0; //최대대기시간을 재설정

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
                    order.setTotalWaitTimeMillis(totalWaitTimeMillis);
                    ordersQueue.add(order);
                    currentCount++;
                    if (totalWaitTimeMillis > maxWaitingTimeMillis) {
                        maxWaitingTimeMillis = totalWaitTimeMillis;
                    }
                }
                else { // 주문 완료 시간이 현재 시간을 지났을 경우
                    iterator.remove();
                    ordersQueue.remove(order);
                    if (currentCount > 0) {
                        currentCount--;
                    }
                }
            }
        }
        if (!ordersQueue.isEmpty()) {
            maxWaitingTimeMillis = ordersQueue.peek().getTotalWaitTimeMillis();
        } else {
            maxWaitingTimeMillis = 0;
        }
        totalCount = currentCount; // 현재 계산된 대기 수를 totalCount에 반영
        updateUIWithCurrentCount(); // UI 업데이트
        updateWaitingTimeUI();
    }
    private long getOrderTimeMillis(Orders order) {
        Calendar orderCalendar = Calendar.getInstance();
        Orders.Time orderTime = order.getTime();
        orderCalendar.set(Calendar.HOUR_OF_DAY, orderTime.getHour());
        orderCalendar.set(Calendar.MINUTE, orderTime.getMinute());
        orderCalendar.set(Calendar.SECOND, orderTime.getSecond());
        return orderCalendar.getTimeInMillis();
    }

    private long calculateTotalWaitTimeMillis(Orders order) {
        return order.getMenu().stream()
                .mapToLong(item -> item.getQuantity() * 2 * 60 * 1000) // 각 메뉴 아이템의 대기 시간을 계산
                .sum();
    }


    private void updateWaitingTimeUI() {
        EditText waitingTimeEditText = findViewById(R.id.waitingTime);
        int maxWaitingTimeMinutes = (int) (maxWaitingTimeMillis/60000); //밀리초를 분으로 바꾸기
        int additionalMinutes = 0;

        //대기번호에 따른 추가 시간 계산
        if (totalCount > 6) {
            additionalMinutes = 10;
        } else if (totalCount > 2) {
            additionalMinutes = 5;
        }
        int totalWaitingTime = maxWaitingTimeMinutes + additionalMinutes;
        waitingTimeEditText.setText(String.valueOf(totalWaitingTime));
    }

    /*private void updateUIWithClosed() {
        String closeText = "영업종료";
        tv_waitingNumber.setText(closeText);
        textViewSuffix.setVisibility(View.GONE);
        saveTotalCount(0); // 영업 종료 시 혼잡도 상태를 녹색으로
    }*/

    public void addOrder(Orders newOrder){
        // 새 주문을 주문 리스트에 추가
        ordersList.add(newOrder);
        long newOrderWaitTime = calculateTotalWaitTimeMillis(newOrder);
        newOrder.setTotalWaitTimeMillis(newOrderWaitTime);
        ordersQueue.add(newOrder);
        // 대기번호 증가
        totalCount++;
        // UI 업데이트
        updateUIWithCurrentCount();
    }


    private void updateUIWithCurrentCount() {
        tv_waitingNumber.setText(Integer.toString(totalCount));
        textViewSuffix.setVisibility(View.VISIBLE);
        saveTotalCount(totalCount); // 혼잡도 상태 저장
        // Firebase에 totalCount 업데이트 추가
        updateTotalCountInFirebase(totalCount);

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
        editor.putString("CongestionStatus", status);
        editor.apply();
    }
    private void saveWaitingInfo(int waitingNumber, int maxWaitingTime) {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("WaitingNumber", waitingNumber);
        editor.putInt("MaxWaitingTime", maxWaitingTime);
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

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 시간 변경 로직
                String newTime = editTime.getText().toString();
                // 로직 구현...
                dialog.dismiss();
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