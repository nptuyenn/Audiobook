package com.example.audiobook_for_kids;

import android.content.Intent;
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
        tvRating = findViewById(R.id.tv_rating);
        tvDescription = findViewById(R.id.tv_description);
        btnPlay = findViewById(R.id.btn_play);
        rvEpisodes = findViewById(R.id.rv_episodes);
    }

    private void loadDataFromIntent() {
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy dữ liệu sách từ intent
            String bookTitle = intent.getStringExtra("book_title");
            String bookAuthor = intent.getStringExtra("book_author");
            String bookCover = intent.getStringExtra("book_cover");
            String bookDescription = intent.getStringExtra("book_description");

            // Hiển thị dữ liệu nếu có
            if (bookTitle != null) {
                tvTitle.setText(bookTitle);
            } else {
                tvTitle.setText("Alice's Adventures in Wonderland");
            }

            if (bookAuthor != null) {
                tvAuthor.setText("Tác giả: " + bookAuthor);
            } else {
                tvAuthor.setText("Tác giả:  Lewis Carroll");
            }

            if (bookDescription != null && !bookDescription.isEmpty()) {
                tvDescription.setText(bookDescription);
            } else {
                tvDescription.setText(
                    "Alice tình cờ đuổi theo một chú thỏ trắng biết nói và rơi vào Xứ Sở Thần Tiên, nơi cô trải qua hàng loạt cuộc phiêu lưu kỳ quái: " +
                            "lúc to lúc nhỏ, gặp mèo Cheshire bí ẩn, tiệc trà điên loạn và Nữ Hoàng Đỏ thích ra lệnh chém đầu. " +
                            "Sau khi bị cuốn vào một phiên tòa hỗn loạn, Alice bất ngờ tỉnh dậy và nhận ra tất cả chỉ là một giấc mơ lạ lùng.\n\n" +
                            "Bài học: Câu chuyện nhắc ta hãy giữ sự tò mò và bản sắc riêng của mình trong một thế giới đầy điều vô lý và biến đổi."
                );
            }
        }

        // Hiển thị thông tin mặc định khác
        tvDuration.setText("12 phút");
        tvRating.setText("4.8 ⭐");
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
        try {
            Intent intent = new Intent(this, PlayerActivity.class);

            // Truyền dữ liệu sách sang PlayerActivity
            Intent currentIntent = getIntent();
            if (currentIntent != null) {
                intent.putExtra("book_title", currentIntent.getStringExtra("book_title"));
                intent.putExtra("book_author", currentIntent.getStringExtra("book_author"));
                intent.putExtra("book_cover", currentIntent.getStringExtra("book_cover"));
                intent.putExtra("book_id", currentIntent.getStringExtra("book_id"));
            } else {
                // Dữ liệu mặc định nếu không có intent
                intent.putExtra("book_title", tvTitle.getText().toString());
                intent.putExtra("book_author", tvAuthor.getText().toString().replace("Tác giả: ", ""));
                intent.putExtra("book_cover", "");
                intent.putExtra("book_id", "1");
            }

            startActivity(intent);

            // Hiệu ứng: PlayerActivity trượt lên từ dưới, trang hiện tại giữ nguyên
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);

        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trình phát audio: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
