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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private ArrayList<String> keyList;
    private Context context;
    private DatabaseReference databaseReference;

    public CommentAdapter(DatabaseReference databaseReference, List<Comment> commentList, ArrayList<String> keyList, Context context) {
        this.databaseReference = databaseReference;
        this.commentList = commentList;
        this.keyList = keyList;
        this.context = context;
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
        holder.tvCommentAuthor.setText(comment.getUserId());

        holder.iconMore.setOnClickListener(view -> showPopupMenu(view, position, comment.getContent()));
    }

    private String getReadableTimestamp(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return formatter.format(new Date(timestamp));
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
                    // Firebase에서만 삭제하고 RecyclerView에서는 제거하지 않음
                    Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Invalid position
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
                    Toast.makeText(context, "댓글이 수정되었습니다..", Toast.LENGTH_SHORT).show();
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