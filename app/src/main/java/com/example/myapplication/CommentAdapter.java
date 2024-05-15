package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private ArrayList<String> keyList;
    private Context context;
    private DatabaseReference databaseReference;
    private Map<String, String> userNicknames; // UID와 닉네임을 매핑하기 위한 맵

    public CommentAdapter(DatabaseReference databaseReference, List<Comment> commentList, ArrayList<String> keyList, Context context) {
        this.databaseReference = databaseReference;
        this.commentList = commentList;
        this.keyList = keyList;
        this.context = context;
        this.userNicknames = new HashMap<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvComment.setText(comment.getContent());
        holder.tvCommentTimestamp.setText(getReadableTimestamp(comment.getTimestamp()));
        String userId = comment.getUserId();

        if (userNicknames.containsKey(userId)) {
            holder.tvCommentAuthor.setText(userNicknames.get(userId));
        } else {
            loadUserNickname(userId, holder.tvCommentAuthor);
        }

        holder.iconMore.setOnClickListener(view -> showPopupMenu(view, position, comment.getContent()));
    }

    private void loadUserNickname(String userId, TextView textView) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nickname = snapshot.child("Nickname").getValue(String.class);
                    userNicknames.put(userId, nickname);
                    textView.setText(nickname);
                } else {
                    // Users에서 닉네임을 찾을 수 없는 경우 CeoUsers에서 확인
                    DatabaseReference ceoUserRef = FirebaseDatabase.getInstance().getReference("CeoUsers").child(userId);
                    ceoUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot ceoSnapshot) {
                            if (ceoSnapshot.exists()) {
                                String nickname = ceoSnapshot.child("Nickname").getValue(String.class);
                                userNicknames.put(userId, nickname);
                                textView.setText(nickname);
                            } else {
                                textView.setText("Unknown");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            textView.setText("Unknown");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                textView.setText("Unknown");
            }
        });
    }

    private String getReadableTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 한국 시간대로 설정
        return sdf.format(new Date(timestamp));
    }

    private void showPopupMenu(View view, final int position, String content) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.comment_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_comment) {
                showOptionsDialog(position, content);
                return true;
            } else if (item.getItemId() == R.id.action_delete_comment) {
                deleteComment(position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteComment(int position) {
        if (position >= 0 && position < commentList.size() && position < keyList.size()) {
            String commentKey = keyList.get(position);
            databaseReference.child(commentKey).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Invalid position.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOptionsDialog(final int position, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        EditText input = new EditText(context);
        input.setText(content);
        builder.setView(input);

        builder.setTitle("댓글 수정");
        builder.setItems(new CharSequence[]{"수정", "삭제", "취소"}, (dialog, which) -> {
            if (which == 0) {
                editComment(position, input.getText().toString());
            } else if (which == 1) {
                deleteComment(position);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void editComment(int position, String newContent) {
        if (position < keyList.size()) {
            String commentKey = keyList.get(position);
            Map<String, Object> updates = new HashMap<>();
            updates.put("content", newContent);
            databaseReference.child(commentKey).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    commentList.get(position).setContent(newContent);
                    notifyItemChanged(position);
                    Toast.makeText(context, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvComment;
        TextView tvCommentAuthor;
        TextView tvCommentTimestamp;
        ImageView iconMore;

        CommentViewHolder(View itemView) {
            super(itemView);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvCommentAuthor = itemView.findViewById(R.id.tvCommentAuthor);
            tvCommentTimestamp = itemView.findViewById(R.id.tvCommentTimestamp);
            iconMore = itemView.findViewById(R.id.iconMore);
        }
    }
}
