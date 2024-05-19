package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

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


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(MypageActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    startActivity(new Intent(MypageActivity.this, NotificationActivity.class));
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(MypageActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우

            }
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
}

