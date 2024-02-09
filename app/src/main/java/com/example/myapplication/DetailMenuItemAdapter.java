package com.example.myapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;

public class DetailMenuItemAdapter extends ListAdapter<DetailpageActivity.DetailMenuItem, RecyclerView.ViewHolder> {
    public final static int VIEW_TYPE_HEADER = 0;
    public final static int VIEW_TYPE_MENU_ITEM = 1;

    public DetailMenuItemAdapter(@NonNull DiffUtil.ItemCallback<DetailpageActivity.DetailMenuItem> diffCallback) {
        super(diffCallback);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    // 바인딩 처리
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if(viewType == VIEW_TYPE_HEADER) {
            holder = new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_detail_header, parent, false));
        } else {
            holder = new DetailMenuItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_detail_menu_item, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder)holder).bind();
        }
        else if(holder instanceof DetailMenuItemViewHolder) {
            ((DetailMenuItemViewHolder)holder).bind(getItem(position));
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View view) {
            super(view);
        }

        @SuppressLint("SetTextI18n")
        private void bind() {
            // 헤더명
        }
    }

    static class DetailMenuItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvOptions;
        private final TextView tvKcalSugar;

        public DetailMenuItemViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvPrice = view.findViewById(R.id.tv_price);
            tvOptions = view.findViewById(R.id.tv_options);
            tvKcalSugar = view.findViewById(R.id.tv_kcalSugar);
        }

        @SuppressLint("SetTextI18n")
        private void bind(DetailpageActivity.DetailMenuItem menuItem) {
            // 메뉴명
            tvTitle.setText(menuItem.getTitle());
            // 메뉴 가격
            String priceWon = NumberFormat.getInstance().format(menuItem.getPrice());
            tvPrice.setText(priceWon + "원");
            // 메뉴 옵션
            if(menuItem.getOptions().isEmpty()) {
                tvOptions.setVisibility(View.GONE);
            } else {
                tvOptions.setText(menuItem.getOptions().toString());
                tvOptions.setVisibility(View.VISIBLE);
            }
            // 칼로리, 당류
            tvKcalSugar.setText("칼로리: " + menuItem.getCalorie() + ", 당류: " + menuItem.getSugar());
        }
    }

    public static class DetailMenuItemDiffUtil extends DiffUtil.ItemCallback<DetailpageActivity.DetailMenuItem> {
        @Override
        public boolean areItemsTheSame(@NonNull DetailpageActivity.DetailMenuItem oldItem, @NonNull DetailpageActivity.DetailMenuItem newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DetailpageActivity.DetailMenuItem oldItem, @NonNull DetailpageActivity.DetailMenuItem newItem) {
            return oldItem.equals(newItem);
        }
    }
}



