package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import android.text.style.StyleSpan;


public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // buttonMap에 클릭 리스너 추가
        Button buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // ImageView 찾기
        ImageView alarmLogo = findViewById(R.id.alarm_logo);

        // ImageView에 클릭 리스너 추가
        alarmLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NotificationActivity로 이동하는 Intent 생성
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });



        // TextView에 SpannableString 적용
        TextView textView = findViewById(R.id.textViewCertainU);
        String fullText = "CertaIN U";
        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf("IN U");
        int end = start + "IN U".length();

        // 색상 변경
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 굵은 글씨
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);





        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.nav_item_color, null));
        resetIcons(); // 초기 상태 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            resetIcons(); // 모든 아이콘을 회색으로 설정
            int itemId = item.getItemId();

            if (itemId == R.id.action_home) {
                item.setIcon(R.drawable.bottom_home_black); // 선택된 아이콘으로 변경
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                item.setIcon(R.drawable.bottom_notification_black);
                startActivity(new Intent(MainActivity.this, CeoBoardActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                item.setIcon(R.drawable.bottom_writeboard_black);
                startActivity(new Intent(MainActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                item.setIcon(R.drawable.bottom_mypage_black);
                startActivity(new Intent(MainActivity.this, MypageActivity.class));
                return true;
            }

            return false;
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

    private void resetIcons() {
        // 메뉴 아이템을 찾아 회색 아이콘으로 설정
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.action_home).setIcon(R.drawable.bottom_home_black);
        menu.findItem(R.id.action_notification).setIcon(R.drawable.bottom_notification_gray);
        menu.findItem(R.id.action_board).setIcon(R.drawable.bottom_writeboard_gray);
        menu.findItem(R.id.action_mypage).setIcon(R.drawable.bottom_mypage_gray);
    }
}