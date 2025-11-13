package com.example.audiobook_for_kids;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

public class AudiobookDetailActivity extends AppCompatActivity {

    private ImageView ivCover;
    private ImageView btnBack;
    private ImageView btnFavorite;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDuration;
    private TextView tvAge;
    private TextView tvRating;
    private TextView tvDescription;
    private MaterialButton btnPlay;
    private RecyclerView rvEpisodes;

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_detail);

        // Ánh xạ views
        initViews();

        // Nhận dữ liệu từ Intent
        loadDataFromIntent();

        // Xử lý sự kiện
        setupClickListeners();
    }

    private void initViews() {
        ivCover = findViewById(R.id.iv_cover);
        btnBack = findViewById(R.id.btn_back);
        btnFavorite = findViewById(R.id.btn_favorite);
        tvTitle = findViewById(R.id.tv_title);
        tvAuthor = findViewById(R.id.tv_author);
        tvDuration = findViewById(R.id.tv_duration);
        tvAge = findViewById(R.id.tv_age);
        tvRating = findViewById(R.id.tv_rating);
        tvDescription = findViewById(R.id.tv_description);
        btnPlay = findViewById(R.id.btn_play);
        rvEpisodes = findViewById(R.id.rv_episodes);
    }

    private void loadDataFromIntent() {
        // Nhận dữ liệu từ Intent
        int coverResId = getIntent().getIntExtra("COVER_RES_ID", 0);

        // Hiển thị ảnh bìa nếu có
        if (coverResId != 0) {
            ivCover.setImageResource(coverResId);
        }

        // TODO: Khi có backend, nhận bookId và gọi API để lấy dữ liệu đầy đủ
        // String bookId = getIntent().getStringExtra("BOOK_ID");
        // loadBookDetails(bookId);

        // Dữ liệu mẫu (thay bằng dữ liệu thực tế từ API)
        tvTitle.setText("Cáo và Quạ");
        tvAuthor.setText("Tác giả: Ngụ ngôn Aesop");
        tvDuration.setText("12 phút");
        tvAge.setText("3-6 tuổi");
        tvRating.setText("4.8 ⭐");
        tvDescription.setText(
            "Câu chuyện kể về một con quạ đậu trên cành cây với miếng phô mai trong mỏ. " +
            "Con cáo tinh ranh muốn lấy miếng phô mai nên đã nịnh hót con quạ. " +
            "Khi quạ cất tiếng hót, miếng phô mai rơi xuống và cáo đã ăn mất.\n\n" +
            "Bài học: Đừng tin vào lời nịnh hót và luôn cảnh giác với người có ý đồ xấu."
        );
    }

    private void setupClickListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút yêu thích
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Nút phát truyện
        btnPlay.setOnClickListener(v -> playAudiobook());
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            // TODO: Lưu vào database hoặc gọi API
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            // TODO: Xóa khỏi database hoặc gọi API
        }
    }

    private void playAudiobook() {
        // TODO: Mở màn hình player hoặc bắt đầu phát audio
        Toast.makeText(this, "Bắt đầu phát truyện...", Toast.LENGTH_SHORT).show();

        // Ví dụ: Mở PlayerActivity
        // Intent intent = new Intent(this, PlayerActivity.class);
        // intent.putExtra("BOOK_ID", bookId);
        // startActivity(intent);
    }
}
