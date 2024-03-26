package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void onBindViewHolder(@NonNull CommentViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Comment comment = commentList.get(position);
        holder.tvComment.setText(comment.getContent());

        holder.iconMore.setOnClickListener(view -> showPopupMenu(view, position, comment.getContent()));
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
                    Toast.makeText(context, "Comment deleted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete comment.", Toast.LENGTH_SHORT).show();
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

        builder.setTitle("Edit Comment");
        builder.setItems(new CharSequence[]{"Edit", "Delete", "Cancel"}, (dialog, which) -> {
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
                    Toast.makeText(context, "Comment edited successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to edit comment.", Toast.LENGTH_SHORT).show();
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
        ImageView iconMore;

        CommentViewHolder(View itemView) {
            super(itemView);
            tvComment = itemView.findViewById(R.id.tvComment);
            iconMore = itemView.findViewById(R.id.iconMore);
        }
    }
}