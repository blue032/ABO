package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEmpty = findViewById(R.id.btn_empty);
        Button btnWaiting = findViewById(R.id.btn_waiting);
        Button btnMap = findViewById(R.id.mapcheckbutton);

        TextView tvNotice = findViewById(R.id.notice);

        TextView tvCafeOO = findViewById(R.id.abo2);
        tvCafeOO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // O.O카페 TextView가 클릭되었을 때 수행할 동작
                Intent intent = new Intent(MainActivity.this, DetailpageActivity.class);
                // 필요한 경우 intent에 추가 데이터를 넣습니다.
                startActivity(intent);
            }
        });

        tvNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CeoBoardActivity로 이동하는 인텐트 생성 및 시작
                Intent intent = new Intent(MainActivity.this, CeoBoardActivity.class);
                startActivity(intent);
            }
        });

        btnEmpty.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
            intent.putExtra("viewType", "emptySeats");
            startActivity(intent);
        });

        btnWaiting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
            intent.putExtra("viewType", "waitingList");
            startActivity(intent);
        });



        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_home) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                startActivity(new Intent(MainActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                startActivity(new Intent(MainActivity.this, NotificationActivity.class)); // 알림 항목 클릭 시 NotificationActivity로 이동
                return true;
            } else if (itemId == R.id.action_mypage) {
                startActivity(new Intent(MainActivity.this, MypageActivity.class));
                return true;
            }
            return false;
        });

        LinearLayout ceoBoardPostContainer = findViewById(R.id.ceoBoardPostContainer);
        DatabaseReference ceoBoardRef = FirebaseDatabase.getInstance().getReference("ceoBoard");

        ceoBoardRef.orderByChild("timestamp").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<CeoBoardPost> tempList = new ArrayList<>();
                ceoBoardPostContainer.removeAllViews();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CeoBoardPost post = postSnapshot.getValue(CeoBoardPost.class);
                    if (post != null) {
                        tempList.add(post);
                    }
                }

                Collections.sort(tempList, Comparator.comparingLong(CeoBoardPost::getTimestamp).reversed());

                for (CeoBoardPost post : tempList) {
                    LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    TextView titleView = new TextView(MainActivity.this);
                    titleView.setText(post.getTitle());
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    titleView.setTextColor(Color.BLACK);
                    titleView.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    TextView dateView = new TextView(MainActivity.this);
                    dateView.setText(post.getFormattedDate());
                    dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    dateView.setTextColor(Color.BLACK);
                    dateView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    dateView.setGravity(Gravity.RIGHT);

                    linearLayout.addView(titleView);
                    linearLayout.addView(dateView);

                    linearLayout.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, CeoDetailActivity.class);
                        intent.putExtra("postId", post.getPostId());
                        intent.putExtra("title", post.getTitle());
                        intent.putExtra("content", post.getContent());
                        startActivity(intent);
                    });

                    ceoBoardPostContainer.addView(linearLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리 코드를 여기에 작성합니다.
            }
        });

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "권한거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앱을 사용하기 위해서는 권한이 필요해요")
                .setDeniedMessage("설정에서 권한을 다시 설정해주세요")
                .setPermissions(
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                        android.Manifest.permission.BLUETOOTH_ADVERTISE,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.READ_CALENDAR,
                        android.Manifest.permission.CAMERA)
                .check();
    }
}