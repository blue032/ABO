package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEmpty = findViewById(R.id.btn_empty);
        Button btnWaiting = findViewById(R.id.btn_waiting);
        Button btnMap = findViewById(R.id.mapcheckbutton);

        btnEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
                intent.putExtra("viewType", "emptySeats");
                startActivity(intent);
            }
        });

        btnWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CafelistActivity.class);
                intent.putExtra("viewType", "waitingList");
                startActivity(intent);
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_board) {
                    // 게시판 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(MainActivity.this, BoardActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_notification) {
                    // 알림 아이템이 선택되었을 때의 동작
                    return true;
                } else if (itemId == R.id.action_mypage) {
                    // 메뉴 페이지 아이템이 선택되었을 때의 동작
                    Intent intent = new Intent(MainActivity.this, MypageActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false; // 아무 항목도 선택되지 않았을 경우

            }
        });

        TextView tvNotice = findViewById(R.id.notice);
        tvNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NoticeActivity로 이동하는 인텐트를 생성합니다.
                Intent intent = new Intent(MainActivity.this, CeoBoardActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout ceoBoardPostContainer = findViewById(R.id.ceoBoardPostContainer);
        DatabaseReference ceoBoardRef = FirebaseDatabase.getInstance().getReference("ceoBoard");
        Button btnCeoBoard = findViewById(R.id.btnCeoBoard);
        btnCeoBoard.setVisibility(View.GONE);

        ceoBoardRef.orderByChild("timestamp").limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BoardPost> tempList = new ArrayList<>();
                ceoBoardPostContainer.removeAllViews(); // 이전에 추가된 뷰들을 제거합니다.

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BoardPost post = postSnapshot.getValue(BoardPost.class);
                    if (post != null) {
                        tempList.add(post);
                    }
                }

                // tempList를 timestamp의 내림차순으로 정렬합니다.
                Collections.sort(tempList, new Comparator<BoardPost>() {
                    @Override
                    public int compare(BoardPost o1, BoardPost o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });

                for (BoardPost post : tempList) {
                    // 각 게시물에 대한 LinearLayout 생성
                    LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                    // 제목을 위한 TextView 생성
                    TextView titleView = new TextView(MainActivity.this);
                    titleView.setText(post.getTitle());
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    titleView.setTextColor(Color.BLACK);
                    titleView.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1f)); // 가중치 1

                    // 날짜를 위한 TextView 생성
                    TextView dateView = new TextView(MainActivity.this);
                    dateView.setText(post.getFormattedDate());
                    dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // 작은 글자 크기
                    dateView.setTextColor(Color.BLACK);
                    dateView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    dateView.setGravity(Gravity.RIGHT);

                    // LinearLayout에 제목과 날짜 TextView 추가
                    linearLayout.addView(titleView);
                    linearLayout.addView(dateView);

                    // 클릭 리스너 추가
                    linearLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 인텐트 생성 및 시작
                            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                            intent.putExtra("title", post.getTitle());
                            intent.putExtra("content", post.getContent());
                            intent.putExtra("timestamp", post.getTimestamp());
                            startActivity(intent);
                        }
                    });

                    // 부모 뷰에 LinearLayout 추가
                    ceoBoardPostContainer.addView(linearLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리 코드를 여기에 작성합니다.
            }
        });


    }
}
