package com.example.myapplication;

import android.Manifest;
import android.animation.ObjectAnimator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker cafeMarker;
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
            if ("CongestionStatus".equals(key)) {
                runOnUiThread(() -> {
                    String status = sharedPreferences.getString(key, "default_status");
                    updateCongestionStatus(status);
                });
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        refreshButton = findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(v -> {
            String status = sharedPreferences.getString("CongestionStatus", "default_status");
            updateCongestionStatus(status);
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
        setupRunnable();
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

        BitmapDescriptor icon = resizeMapIcons("location_green", 100, 100); // Adjust the size as needed

        for (LatLng location : locations) {
            gMap.addMarker(new MarkerOptions().position(location).icon(icon));
        }
        LatLng cafeLocation = new LatLng(37.37452483159567, 126.6332926552895); // O.O 카페 위치
        cafeMarker = googleMap.addMarker(new MarkerOptions().position(cafeLocation).title("O.O 카페").icon(icon));

        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        String currentStatus = prefs.getString("CongestionStatus", "icon_green");
        updateCongestionStatus(currentStatus);

        googleMap.setOnMarkerClickListener(marker -> {
            if ("O.O 카페".equals(marker.getTitle())) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.bottom_sheet_layout, null);

                SharedPreferences cafePrefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
                int waitingNumber = cafePrefs.getInt("WaitingNumber", 0);
                int maxWaitingTime = cafePrefs.getInt("MaxWaitingTime", 0);

                TextView tvOrderCount = dialogLayout.findViewById(R.id.tv_order_count);
                TextView tvWaitTime = dialogLayout.findViewById(R.id.tv_wait_time);

                tvOrderCount.setText(String.format("%d건", waitingNumber));
                tvWaitTime.setText(String.format("%d분 예상", maxWaitingTime));

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

    private BitmapDescriptor resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
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
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        String status = prefs.getString("CongestionStatus", "icon_green");
        updateCongestionStatus(status);
    }

    private void updateCongestionStatus(String status) {
        BitmapDescriptor iconDescriptor;
        switch(status) {
            case "icon_green":
                iconDescriptor = resizeMapIcons("location_green", 100, 100);
                break;
            case "icon_blue":
                iconDescriptor = resizeMapIcons("location_blue", 100, 100);
                break;
            case "icon_red":
                iconDescriptor = resizeMapIcons("location_red", 100, 100);
                break;
            default:
                iconDescriptor = BitmapDescriptorFactory.defaultMarker();
                break;
        }

        if (cafeMarker != null) {
            cafeMarker.setIcon(iconDescriptor);
        }

        ImageView imageView10 = findViewById(R.id.iv_map_top);
        int drawableId = getResources().getIdentifier(status, "drawable", getPackageName());
        imageView10.setImageResource(drawableId);
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
        String referencePath = "Orders/2024/3/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        databaseReference = database.getReference(referencePath);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Orders order = snapshot.getValue(Orders.class);

                    if (order != null) {
                        ordersList.add(order);
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
                checkOrdersTime();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void checkOrdersTime() {
        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

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
            saveWaitingInfo(0, 0);
            updateBottomSheetData(0, 0); // Bottom sheet 업데이트
            return;
        }

        int currentCount = 0;
        maxWaitingTimeMillis = 0;

        for (Iterator<Orders> iterator = ordersList.iterator(); iterator.hasNext(); ) {
            Orders order = iterator.next();

            long orderTimeMillis = getOrderTimeMillis(order);
            long totalWaitTimeMillis = calculateTotalWaitTimeMillis(order);
            long orderEndTimeMillis = orderTimeMillis + totalWaitTimeMillis;

            if (orderTimeMillis <= nowMillis) {
                if (orderEndTimeMillis > nowMillis) {
                    currentCount++;
                    if (totalWaitTimeMillis > maxWaitingTimeMillis) {
                        maxWaitingTimeMillis = totalWaitTimeMillis;
                    }
                } else {
                    iterator.remove();
                }
            }
        }

        totalCount = currentCount;
        int maxWaitingTimeMinutes = (int) (maxWaitingTimeMillis / 60000);

        int additionalMinutes = 0;
        if (totalCount > 6) {
            additionalMinutes = 10;
        } else if (totalCount > 2) {
            additionalMinutes = 5;
        }
        int totalWaitingTime = maxWaitingTimeMinutes + additionalMinutes;

        saveWaitingInfo(totalCount, totalWaitingTime);
        updateBottomSheetData(totalCount, totalWaitingTime);
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
                .mapToLong(item -> item.getQuantity() * 2 * 60 * 1000)
                .sum();
    }

    private void updateBottomSheetData(int waitingNumber, int maxWaitingTime) {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            TextView tvOrderCount = bottomSheetDialog.findViewById(R.id.tv_order_count);
            TextView tvWaitTime = bottomSheetDialog.findViewById(R.id.tv_wait_time);

            if (tvOrderCount != null) {
                tvOrderCount.setText(String.format("%d건", waitingNumber));
            }
            if (tvWaitTime != null) {
                tvWaitTime.setText(String.format("%d분 예상", maxWaitingTime));
            }
        }
    }

    private void saveWaitingInfo(int waitingNumber, int maxWaitingTime) {
        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("WaitingNumber", waitingNumber);
        editor.putInt("MaxWaitingTime", maxWaitingTime);
        editor.apply();
    }

    private void refreshCafeData(TextView tvOrderCount, TextView tvWaitTime) {
        checkOrdersTime();

        SharedPreferences prefs = getSharedPreferences("CafeStatusPrefs", MODE_PRIVATE);
        String status = prefs.getString("CongestionStatus", "icon_green");
        updateCongestionStatus(status);

        int waitingNumber = prefs.getInt("WaitingNumber", 0);
        int maxWaitingTime = prefs.getInt("MaxWaitingTime", 0);

        tvOrderCount.setText(String.format("%d건", waitingNumber));
        tvWaitTime.setText(String.format("%d분 예상", maxWaitingTime));
    }
}
