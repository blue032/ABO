package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        ImageView backLogo = (ImageView) findViewById(R.id.back_arrow);
        backLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // 현재 액티비티 종료
            }
        });

        // ImageView 찾기
        ImageView alarmLogo = findViewById(R.id.bell);

        // ImageView에 클릭 리스너 추가
        alarmLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NotificationActivity로 이동하는 Intent 생성
                Intent intent = new Intent(MypageActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });


        // Firebase 인증 및 데이터베이스 초기화
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        TextView emailTextView = findViewById(R.id.email);
        TextView nameTextView = findViewById(R.id.username);

        // 현재 로그인한 사용자의 정보를 가져옵니다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            emailTextView.setText(userEmail); // 이메일 설정

            // 데이터베이스에서 사용자 정보 조회
            mDatabase.child("Users").orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String name = userSnapshot.child("name").getValue(String.class);
                            nameTextView.setText(name); // 사용자 이름 설정
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 데이터베이스 오류 처리
                }
            });

            // 사장님 정보 조회
            mDatabase.child("CeoUsers").orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot ceoUserSnapshot : dataSnapshot.getChildren()) {
                            String ceoName = ceoUserSnapshot.child("name").getValue(String.class);
                            nameTextView.setText(ceoName); // 사장님 이름 설정
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 데이터베이스 오류 처리
                }
            });
        }


        // 학교 홈페이지로 이동하는 버튼
        // 대학교 페이지로 이동하는 이미지뷰 버튼
        ImageView btnSchoolPage = findViewById(R.id.btn_university);
        btnSchoolPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.inu.ac.kr/inu/index.do?epTicket=LOG"));
                startActivity(browserIntent);
            }
        });


        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setItemIconTintList(null);
        resetIcons(); // 초기 상태 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            resetIcons(); // 모든 아이콘을 회색으로 설정
            int itemId = item.getItemId();

            if (itemId == R.id.action_home) {
                item.setIcon(R.drawable.bottom_home_black); // 선택된 아이콘으로 변경
                startActivity(new Intent(MypageActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.action_notification) {
                item.setIcon(R.drawable.bottom_notification_black);
                startActivity(new Intent(MypageActivity.this, CeoBoardActivity.class));
                return true;
            } else if (itemId == R.id.action_board) {
                item.setIcon(R.drawable.bottom_writeboard_black);
                startActivity(new Intent(MypageActivity.this, BoardActivity.class));
                return true;
            } else if (itemId == R.id.action_mypage) {
                item.setIcon(R.drawable.bottom_mypage_black);
                startActivity(new Intent(MypageActivity.this, MypageActivity.class));
                return true;
            }

            return false;
        });

        // 로그아웃 버튼
        TextView btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그아웃 로직 (Firebase Authentication 로그아웃)
                mAuth.signOut();
                Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 비밀번호 재설정 페이지로 이동하는 버튼
        // 비밀번호 재설정 페이지로 이동하는 이미지뷰 버튼
        ImageView btnResetPassword = findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, PwSearchActivity.class);
                startActivity(intent);
            }
        });

    }
    private void resetIcons() {
        // 메뉴 아이템을 찾아 회색 아이콘으로 설정
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(R.id.action_home).setIcon(R.drawable.bottom_home_black);
        menu.findItem(R.id.action_notification).setIcon(R.drawable.bottom_notification_black);
        menu.findItem(R.id.action_board).setIcon(R.drawable.bottom_writeboard_black);
        menu.findItem(R.id.action_mypage).setIcon(R.drawable.bottom_mypage_black);
    }
}

