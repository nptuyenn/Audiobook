package com.example.audiobook_for_kids;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AudiobookAdapter extends RecyclerView.Adapter<AudiobookAdapter.ViewHolder> {

    private final Context context;
    // Dùng Integer để tham chiếu đến R.drawable. Dùng String nếu bạn tải từ URL (với Glide/Picasso)
    private final List<Integer> coverDrawables;

    // Constructor
    public AudiobookAdapter(Context context, List<Integer> coverDrawables) {
        this.context = context;
        this.coverDrawables = coverDrawables;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "Thổi phồng" (inflate) layout item_audiobook_cover.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook_cover, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy dữ liệu (ID drawable) tại vị trí 'position'
        int coverResId = coverDrawables.get(position);

        // Gán hình ảnh cho ImageView
        holder.coverImageView.setImageResource(coverResId);

        // (Bạn có thể thêm OnClickListener ở đây)
        holder.itemView.setOnClickListener(v -> {
            // Xử lý khi bé nhấn vào một cuốn truyện
        });
    }

    @Override
    public int getItemCount() {
        return coverDrawables.size();
    }

    // Lớp ViewHolder để "giữ" các view của item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.iv_cover);
        }
    }
}