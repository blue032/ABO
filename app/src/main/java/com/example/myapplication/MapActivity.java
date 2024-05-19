package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TimeZone;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker cafeMarker;
    private List<Marker> locationMarkers = new ArrayList<>(); // 나머지 위치의 마커를 저장할 리스트
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private Button refreshButton;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private Handler handler = new Handler();
    private Runnable runnable;
    private ArrayList<Orders> ordersList = new ArrayList<>();
    private long maxWaitingTimeMillis = 0;
    private int totalCount = 0;
    private BottomSheetDialog bottomSheetDialog;
    private long estimatedWaitTimeMillis = -1; // 처음에는 -1로 설정
    private int previousTotalCount = -1; // 이전 totalCount 값

    private TextView tv_wait_time;  // UI에 추가한 예상 대기 시간을 표시할 TextView
    private TextView tv_order_count;  // UI에 추가한 주문 수를 표시할 TextView

    private PriorityQueue<Orders> ordersQueue = new PriorityQueue<>(new Comparator<Orders>() {
        @Override
        public int compare(Orders o1, Orders o2) {
            return Long.compare(o2.getTotalWaitTimeMillis(), o1.getTotalWaitTimeMillis());
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.map);
        initGoogleMap(savedInstanceState);

        sharedPreferences = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);

        preferenceChangeListener = (sharedPreferences, key) -> {
            if ("CongestionStatus".equals(key) || "MaxWaitingTime".equals(key)) {
                runOnUiThread(() -> {
                    updateCongestionStatus();
                });
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        refreshButton = findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(v -> {
            updateCongestionStatus();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(MapActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(MapActivity.this, MypageActivity.class));
                return true;
            }
            return false;
        });

        setupFirebaseListener();
        setupRunnable();  // Runnable 설정 추가
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        LatLng incheonUniversity = new LatLng(37.37502537368127, 126.63272006791813);
        gMap.addMarker(new MarkerOptions().position(incheonUniversity).title("Incheon University"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(incheonUniversity, 16));
        checkLocationPermission();

        List<LatLng> locations = Arrays.asList(
                new LatLng(37.37500867159571, 126.63387174042461),
                new LatLng(37.37591543320427, 126.63281734018747),
                new LatLng(37.372401288059535, 126.6313160023207),
                new LatLng(37.37340586676641, 126.62985469283342),
                new LatLng(37.37439777449398, 126.63154896625312)
        );

        BitmapDescriptor greenIcon = resizeMapIcons(R.drawable.location_green, 130, 130); // Adjust the size as needed

        for (LatLng location : locations) {
            Marker marker = gMap.addMarker(new MarkerOptions().position(location).icon(greenIcon));
            locationMarkers.add(marker);
        }
        LatLng cafeLocation = new LatLng(37.37452483159567, 126.6332926552895); // O.O 카페 위치
        cafeMarker = googleMap.addMarker(new MarkerOptions().position(cafeLocation).title("O.O 카페"));

        updateCongestionStatus();
        updateMarkersBasedOnTime();

        googleMap.setOnMarkerClickListener(marker -> {
            if ("O.O 카페".equals(marker.getTitle())) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.bottom_sheet_layout, null);

                SharedPreferences cafePrefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
                int waitingNumber = cafePrefs.getInt("WaitingNumber", 0);
                long estimatedWaitTimeMillis = cafePrefs.getLong("EstimatedWaitTimeMillis", 0); // EstimatedWaitTimeMillis로 변경

                TextView tvOrderCount = dialogLayout.findViewById(R.id.tv_order_count);
                TextView tvWaitTime = dialogLayout.findViewById(R.id.tv_wait_time);

                tvOrderCount.setText(String.format("%d건", waitingNumber));
                tvWaitTime.setText(String.format("%d분 예상", estimatedWaitTimeMillis / (60 * 1000))); // 밀리초를 분으로 변환

                ImageView reloadIcon = dialogLayout.findViewById(R.id.reload_icon);
                reloadIcon.setOnClickListener(v -> {
                    ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(reloadIcon, "rotation", 0f, 360f);
                    rotateAnimator.setDuration(1000);
                    rotateAnimator.start();

                    refreshCafeData(tvOrderCount, tvWaitTime);
                });

                bottomSheetDialog = new BottomSheetDialog(MapActivity.this);
                bottomSheetDialog.setContentView(dialogLayout);
                bottomSheetDialog.show();
                return true;
            }
            return false;
        });
    }

    private BitmapDescriptor resizeMapIcons(int resId, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Bitmap resizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resizedBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(imageBitmap, null, new android.graphics.Rect(0, 0, width, height), paint);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치 권한 거부됨")
                        .setMessage("위치 권한 없이는 현재 위치를 지도에 표시할 수 없습니다. 앱은 계속 작동하지만, 이 기능은 비활성화됩니다.")
                        .setPositiveButton("확인", (dialog, which) -> ActivityCompat.requestPermissions(MapActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            gMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("위치 권한 거부됨")
                        .setMessage("위치 권한 없이는 현재 위치를 지도에 표시할 수 없습니다. 앱은 계속 작동하지만, 이 기능은 비활성화됩니다.")
                        .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        updateCongestionStatus();
        updateMarkersBasedOnTime(); // 위치 아이콘 업데이트
    }

    private void updateCongestionStatus() {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        long estimatedWaitTimeMillis = prefs.getLong("EstimatedWaitTimeMillis", 0); // 밀리초로 변경

        // 디버깅 로그 추가
        Log.d("updateCongestionStatus", "Estimated wait time (ms): " + estimatedWaitTimeMillis);

        BitmapDescriptor iconDescriptor;
        if (estimatedWaitTimeMillis <= 8 * 60 * 1000) { // 8분 이하
            iconDescriptor = resizeMapIcons(R.drawable.location_green, 130, 130);
            Log.d("updateCongestionStatus", "Setting icon to green");
        } else if (estimatedWaitTimeMillis >= 20 * 60 * 1000) { // 20분 이상
            iconDescriptor = resizeMapIcons(R.drawable.location_red, 130, 130);
            Log.d("updateCongestionStatus", "Setting icon to red");
        } else {
            iconDescriptor = resizeMapIcons(R.drawable.location_blue, 130, 130);
            Log.d("updateCongestionStatus", "Setting icon to blue");
        }

        if (cafeMarker != null) {
            cafeMarker.setIcon(iconDescriptor);
            Log.d("updateCongestionStatus", "Marker icon updated");
        } else {
            Log.e("updateCongestionStatus", "Cafe marker is null");
        }
    }

    private void updateMarkersBasedOnTime() {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        int hour = now.get(Calendar.HOUR_OF_DAY);

        BitmapDescriptor grayIcon = resizeMapIcons(R.drawable.location_gray, 130, 130);

        if (hour < 9 || hour >= 20) { // 오전 9시 이전 또는 오후 8시 이후
            if (cafeMarker != null) {
                cafeMarker.setIcon(grayIcon);
            }
            for (Marker marker : locationMarkers) {
                marker.setIcon(grayIcon);
            }
            Log.d("updateMarkersBasedOnTime", "All markers set to gray");
        } else {
            // 카페 위치 마커는 영업시간에 기존 상태를 유지하고, 나머지 위치는 green으로 설정
            updateCongestionStatus();
            BitmapDescriptor greenIcon = resizeMapIcons(R.drawable.location_green, 130, 130);
            for (Marker marker : locationMarkers) {
                marker.setIcon(greenIcon);
            }
            Log.d("updateMarkersBasedOnTime", "Markers set to green for locations and congestion status for cafe");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
                checkOrdersTime(); // 최대대기시간 업뎃
                calculateEstimatedWaitTime(); // 예상 대기 시간을 계산하고 UI 업데이트
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

    private void checkOrdersTime() {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        // 영업시간 설정
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 1);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        long startTimeMillis = startTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);
        long endTimeMillis = endTime.getTimeInMillis();

        if (nowMillis < startTimeMillis || nowMillis > endTimeMillis) {
            saveWaitingInfo(0, 0); // 영업 종료 시 대기 번호와 대기 시간을 0으로 설정
            updateBottomSheetData(0, 0); // Bottom sheet 업데이트
            estimatedWaitTimeMillis = 0; // 추가: 예상 대기 시간도 초기화
            previousTotalCount = 0;
            return;
        }

        int currentCount = 0;


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
                } else { // 주문 완료 시간이 현재 시간을 지났을 경우
                    iterator.remove();
                }
            }
        }

        if (currentCount != previousTotalCount) { // totalCount가 변경된 경우에만 업데이트
            totalCount = currentCount;
            previousTotalCount = totalCount;
            calculateEstimatedWaitTime();
        }

        // 디버깅 로그 추가
        Log.d("checkOrdersTime", "Current count: " + currentCount + ", Estimated wait time (ms): " + estimatedWaitTimeMillis);

        saveWaitingInfo(totalCount, estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
        updateBottomSheetData(totalCount, estimatedWaitTimeMillis / (60 * 1000)); // Bottom sheet 업데이트
    }

    private void calculateEstimatedWaitTime() {
        if (totalCount == 0) {
            estimatedWaitTimeMillis = 0;
        } else if (totalCount <= 2) {
            estimatedWaitTimeMillis = getRandomNumber(2 * 60 * 1000, 3 * 60 * 1000);
        } else if (totalCount <= 4) {
            estimatedWaitTimeMillis = getRandomNumber(4 * 60 * 1000, 5 * 60 * 1000);
        } else if (totalCount <= 9) {
            estimatedWaitTimeMillis = getRandomNumber(6 * 60 * 1000, 10 * 60 * 1000);
        } else if (totalCount <= 15) {
            estimatedWaitTimeMillis = getRandomNumber(10 * 60 * 1000, 15 * 60 * 1000);
        } else {
            estimatedWaitTimeMillis = getRandomNumber(15 * 60 * 1000, 25 * 60 * 1000);
        }

        saveWaitingInfo(totalCount, estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
    }

    private long getRandomNumber(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    private long getOrderTimeMillis(Orders order) {
        Calendar orderCalendar = Calendar.getInstance();
        Orders.Time orderTime = order.getTime();
        orderCalendar.set(Calendar.HOUR_OF_DAY, orderTime.getHour());
        orderCalendar.set(Calendar.MINUTE, orderTime.getMinute());
        orderCalendar.set(Calendar.SECOND, orderTime.getSecond());
        return orderCalendar.getTimeInMillis();
    }

    private void updateTotalCountInFirebase(int newCount) {
        // 변경될 totalCount 값과 함께 업데이트할 경로를 설정합니다.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());
        DatabaseReference countRef = database.getReference("dailyStats/" + todayDate + "/totalWaitCount");

        // setValue 메소드를 사용하여 데이터베이스에 새 대기번호를 저장합니다.
        countRef.setValue(newCount).addOnSuccessListener(aVoid -> {
            Log.d("MapActivity", "Total count updated successfully for " + todayDate + ": " + newCount);
        }).addOnFailureListener(e -> {
            Log.e("MapActivity", "Failed to update total count for " + todayDate, e);
        });
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

    private void updateBottomSheetData(int waitingNumber, long estimatedWaitTimeMinutes) {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            TextView tvOrderCount = bottomSheetDialog.findViewById(R.id.tv_order_count);
            TextView tvWaitTime = bottomSheetDialog.findViewById(R.id.tv_wait_time);
            TextView tvCongestionStatus = bottomSheetDialog.findViewById(R.id.tv_congestion_status);
            TextView tvRecommendTime = bottomSheetDialog.findViewById(R.id.tv_recommend_time);

            if (tvOrderCount != null) {
                tvOrderCount.setText(String.format("%d건", waitingNumber));
            }
            if (tvWaitTime != null) {
                tvWaitTime.setText(String.format("%d분 예상", estimatedWaitTimeMinutes));
            }

            SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
            int estimatedHour = prefs.getInt("EstimatedVisitHour", 0);
            int estimatedMinute = prefs.getInt("EstimatedVisitMinute", 0);

            if (tvCongestionStatus != null && tvRecommendTime != null) {
                if (waitingNumber == 0 || estimatedWaitTimeMinutes <= 3) {
                    tvCongestionStatus.setText("현재 여유 상태입니다");
                    tvRecommendTime.setText("지금 바로 방문하세요!");
                } else if (estimatedWaitTimeMinutes >= 10) {
                    tvCongestionStatus.setText("현재 혼잡 상태입니다");
                    setRecommendVisitTime(tvRecommendTime);
                } else {
                    tvCongestionStatus.setText("현재 보통 상태입니다");
                    setRecommendVisitTime(tvRecommendTime);
                }
            }
        }
    }

    private void setRecommendVisitTime(TextView tvRecommendTime) {
        // 현재 시간 가져오기
        Calendar now = Calendar.getInstance();

        // 화면에 표시된 대기 번호에 대한 누적 대기 시간을 계산
        long cumulativeWaitTimeMillis = 0;
        int count = 0;

        for (Orders order : ordersList) {
            if (count >= totalCount) break; // 화면에 표시된 대기 번호의 수만큼 계산
            cumulativeWaitTimeMillis += calculateTotalWaitTimeMillis(order);
            count++;
        }

        // 밀리초를 분 단위로 변환
        long cumulativeWaitTimeMinutes = cumulativeWaitTimeMillis / (60 * 1000);

        // 현재 시간에 누적 대기 시간을 분 단위로 더하여 예상 방문 시간 계산
        Calendar estimatedVisitTime = (Calendar) now.clone();
        estimatedVisitTime.add(Calendar.MINUTE, (int) cumulativeWaitTimeMinutes);

        // 예상 방문 시간의 시(hour)와 분(minute) 값을 가져옴
        int hour = estimatedVisitTime.get(Calendar.HOUR_OF_DAY);
        int minute = estimatedVisitTime.get(Calendar.MINUTE);

        // TextView에 예상 방문 시간을 설정
        tvRecommendTime.setText(String.format("%02d시 %02d분에 방문하세요", hour, minute));
    }

    private void saveWaitingInfo(int waitingNumber, long estimatedWaitTimeMillis) {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("WaitingNumber", waitingNumber);
        editor.putLong("EstimatedWaitTimeMillis", estimatedWaitTimeMillis); // 예상 대기 시간을 밀리초로 저장
        editor.apply();
    }

    private void refreshCafeData(TextView tvOrderCount, TextView tvWaitTime) {
        checkOrdersTime();

        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        String status = prefs.getString("CongestionStatus", "icon_green");
        updateCongestionStatus();

        int waitingNumber = prefs.getInt("WaitingNumber", 0);
        long estimatedWaitTimeMillis = prefs.getLong("EstimatedWaitTimeMillis", 0); // 밀리초로 변경

        tvOrderCount.setText(String.format("%d건", waitingNumber));
        tvWaitTime.setText(String.format("%d분 예상", estimatedWaitTimeMillis / (60 * 1000))); // 밀리초를 분으로 변환
    }

}
