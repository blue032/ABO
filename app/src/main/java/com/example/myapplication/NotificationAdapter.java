package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications;

    public NotificationAdapter(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        String notificationText = "새로운 댓글이 있습니다. \"" + notification.getCommentContent() + "\"";
        holder.notificationContent.setText(notificationText);

        // 타임스탬프를 한국 시간으로 변환하여 설정
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String formattedTimestamp = sdf.format(new Date(notification.getTimestamp()));
        holder.notificationTimestamp.setText(formattedTimestamp);

        holder.itemView.setOnClickListener(v -> {
            // Intent를 생성하여 DetailActivity를 시작하고 postId를 전달합니다.
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("postId", notification.getPostId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationContent;
        TextView notificationTimestamp; // 타임스탬프 텍스트뷰

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationContent = itemView.findViewById(R.id.notificationContent);
            notificationTimestamp = itemView.findViewById(R.id.notificationTimestamp); // 텍스트뷰 초기화
        }
    }
}
