// File: adapter/AudiobookAdapter.java
package com.example.audiobook_for_kids.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.audiobook_for_kids.R;
import com.example.audiobook_for_kids.model.Book;
import java.util.List;

public class AudiobookAdapter extends RecyclerView.Adapter<AudiobookAdapter.ViewHolder> {
    private Context context;
    private List<Book> books;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public AudiobookAdapter(Context context, List<Book> books, OnBookClickListener listener) {
        this.context = context;
        this.books = books;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = books.get(position);

        // Hiển thị tên sách
        holder.tvTitle.setText(book.getTitle());

        // Load ảnh bìa với Glide
        Glide.with(context)
            .load(book.getCoverUrl())
            .placeholder(R.color.teal_200) // Màu tạm khi đang load
            .error(R.color.black)          // Màu khi lỗi
            .centerCrop()
            .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;

        ViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}