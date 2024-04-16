package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class CafeMenu_Adapter extends RecyclerView.Adapter<CafeMenu_Adapter.PostViewHolder> {
    private List<CafeMenuPost> posts;

    public CafeMenu_Adapter(List<CafeMenuPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cafemenu_post_item_layout, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CafeMenuPost post = posts.get(position);
        holder.tvTitle.setText(post.getTitle()); // Update this to match your XML IDs
        holder.tvContent.setText(post.getContent());
        // Assuming the ImageView in your XML is for 'iconMore', not for the main content image
        // The main image from the post should have its own ImageView in XML
        if (!post.getPhotoUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getPhotoUrls().get(0)) // Load the first image
                    .into(holder.iconMore); // Update this to the ImageView for the main content image
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle; // This was titleTextView in your provided adapter code
        TextView tvContent; // This was contentTextView in your provided adapter code
        ImageView iconMore; // If you have a separate ImageView for the content, reference it here

        PostViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title); // Changed to the ID from your XML
            tvContent = itemView.findViewById(R.id.tvContent); // Changed to the ID from your XML
            iconMore = itemView.findViewById(R.id.iconMore); // Ensure this ID exists for the content image
            // If you have a separate ImageView for the post's main image, initialize it here as well
        }
    }
}
