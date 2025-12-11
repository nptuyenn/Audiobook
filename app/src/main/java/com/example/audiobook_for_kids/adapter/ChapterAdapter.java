// File: adapter/ChapterAdapter.java
package com.example.audiobook_for_kids.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiobook_for_kids.R;
import com.example.audiobook_for_kids.model.AudioChapter;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {
    private Context context;
    private List<AudioChapter> chapters;
    private OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(AudioChapter chapter);
    }

    public ChapterAdapter(Context context, List<AudioChapter> chapters, OnChapterClickListener listener) {
        this.context = context;
        this.chapters = chapters;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AudioChapter chapter = chapters.get(position);
        holder.tvChapter.setText(chapter.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter));
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapter;
        ViewHolder(View itemView) {
            super(itemView);
            tvChapter = itemView.findViewById(R.id.tv_chapter_title);
        }
    }
}