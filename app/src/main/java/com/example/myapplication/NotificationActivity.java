package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private DatabaseReference notificationsReference;
    private FirebaseAuth mAuth;
    private TextView tvEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        tvEmptyView = findViewById(R.id.tvEmptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        ImageView backLogo = (ImageView) findViewById(R.id.backlogo);
        backLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // 현재 액티비티 종료
            }
        });

        notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        notificationsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                ArrayList<Notification> tempNotifications = new ArrayList<>();
                String currentUserId = mAuth.getCurrentUser().getUid(); // 현재 사용자의 UID 가져오기
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null && currentUserId.equals(notification.getPostUserName())) {
                        notification.setNotificationId(snapshot.getKey()); // 알림 ID 설정
                        tempNotifications.add(notification);
                    }
                }
                // 타임스탬프 내림차순으로 정렬
                Collections.sort(tempNotifications, new Comparator<Notification>() {
                    @Override
                    public int compare(Notification n1, Notification n2) {
                        return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                    }
                });
                notificationList.addAll(tempNotifications);
                checkForEmptyList();
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkForEmptyList() {
        if (notificationList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
