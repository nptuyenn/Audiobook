package com.example.audiobook_for_kids.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.audiobook_for_kids.R;
import com.example.audiobook_for_kids.model.Book;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class TopicAudiobookAdapter extends RecyclerView.Adapter<TopicAudiobookAdapter.ViewHolder> {

    private final List<Book> audiobooks;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Book item);
    }

    public TopicAudiobookAdapter(List<Book> audiobooks, OnItemClickListener listener) {
        this.audiobooks = audiobooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audiobook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book item = audiobooks.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return audiobooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivCover;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        public void bind(Book item, OnItemClickListener listener) {
            // Chỉ set title vì layout item_audiobook chỉ có iv_cover và tv_title
            if (tvTitle != null) {
                tvTitle.setText(item.getTitle());
            }

            // Load image using Glide to handle both URLs and resource URIs
            String coverUrl = item.getCoverUrl();
            if (coverUrl != null && coverUrl.startsWith("android.resource://")) {
                // Handle resource URI
                Uri resourceUri = Uri.parse(coverUrl);
                Glide.with(itemView.getContext())
                        .load(resourceUri)
                        .placeholder(R.drawable.ic_headphone_placeholder)
                        .error(R.drawable.ic_headphone_placeholder)
                        .into(ivCover);
            } else {
                // Handle regular URL or set placeholder
                Glide.with(itemView.getContext())
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_headphone_placeholder)
                        .error(R.drawable.ic_headphone_placeholder)
                        .into(ivCover);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}
