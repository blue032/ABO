package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private  List<Comment> commentList;
    private  ArrayList<String> keyList;
    private  Context context;

    private  DatabaseReference databaseReference;

    public CommentAdapter(DatabaseReference databaseReference,List<Comment> commentList, ArrayList<String> keyList, Context context) {
        this.databaseReference = databaseReference;
        this.commentList = commentList;
        this.keyList = keyList;
        this.context = context;
    }
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View commentView = inflater.inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(commentView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvComment.setText(comment.getContent());

        // 더보기 아이콘 클릭 이벤트 설정
        holder.iconMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view,position,comment.getContent());
            }
        });
    }

    private void showPopupMenu(View view,final int position,String content) {

        Log.e("showPopupMenu" , " commentList size = " + commentList.size());
        Log.e("showPopupMenu" , " keyList size = " + keyList.size());
        Log.e("showPopupMenu" , "position = " + position);
        // 팝업 메뉴 생성 및 옵션 추가
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.comment_menu); // 메뉴 리소스 파일
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                if (item.getItemId() == R.id.action_edit_comment) {
                    // 댓글 수정 코드 추가
                    showOptionsDialog(position,content);
                    return true;
                } else if (item.getItemId() == R.id.action_delete_comment) {
                    // 댓글 삭제 코드 추가
                    deleteComment(position);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void deleteComment(int position) {
        // 댓글 삭제 로직
        databaseReference.child("Comment").child(keyList.get(position)).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                Toast.makeText(context,"삭제를 성공했습니다!",Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(context,"삭제를 실패했습니다!",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void editComment(int position, String content) {

        // 댓글 수정 로직
        // 예: 편집 화면으로 인텐트 전송
    }
    void showOptionsDialog(final int position,String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        EditText input = new EditText(context);
        input.setText(content);
        builder.setView(input);

        builder.setTitle("수정창");
        builder.setItems(new CharSequence[]{"수정", "삭제", "취소"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Edit 버튼 클릭
                        Map<String,Object> map = new HashMap<>();
                        map.put("content",input.getText().toString());

                        databaseReference.child("Comment").child(keyList.get(position)).updateChildren(map).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "수정을 성공했습니다!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "수정을 실패했습니다!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 1: // Delete 버튼 클릭
                        deleteComment(position);
                        break;
                    case 2: // Cancel 버튼 클릭
                        // 아무 작업도 수행하지 않음
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
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
