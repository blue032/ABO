package com.example.myapplication;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
//게시글의 제목 목록을 표시와 스크롤을 가능하도록
public class CeoBoardTitleAdapter extends RecyclerView.Adapter<CeoBoardTitleAdapter.TitleViewHolder> {
    private ArrayList<String> titles;

    public CeoBoardTitleAdapter(ArrayList<String> titles) {
        this.titles = titles;
    }

    @NonNull
    @Override
    public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        // Configure the TextView appearance here
        return new TitleViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
        holder.titleTextView.setText(titles.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        TitleViewHolder(@NonNull TextView textView) {
            super(textView);
            this.titleTextView = textView;
        }
    }
}
