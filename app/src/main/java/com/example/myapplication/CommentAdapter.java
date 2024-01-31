package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
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
        holder.bind(comment);
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

        void bind(final Comment comment) {
            tvComment.setText(comment.getContent());

            // 더보기 아이콘 클릭 이벤트 설정
            iconMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view);
                }
            });
        }

        private void showPopupMenu(View view) {
            // 팝업 메뉴 생성 및 옵션 추가
            PopupMenu popup = new PopupMenu(itemView.getContext(), view);
            popup.inflate(R.menu.comment_menu); // 메뉴 리소스 파일
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    if (item.getItemId() == R.id.action_edit_comment) {
                        // 댓글 수정 코드 추가
                        editComment();
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_comment) {
                        // 댓글 삭제 코드 추가
                        deleteComment();
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        }

        private void editComment() {
            // 댓글 수정 로직
            // 예: 편집 화면으로 인텐트 전송
        }

        private void deleteComment() {
            // 댓글 삭제 로직
            // 예: 데이터베이스에서 댓글 삭제 및 사용자에게 알림 표시
        }
    }
}
