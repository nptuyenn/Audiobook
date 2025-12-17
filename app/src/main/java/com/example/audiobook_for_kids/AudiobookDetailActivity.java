package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;

import com.example.audiobook_for_kids.adapter.ChapterAdapter;
import com.example.audiobook_for_kids.model.AudioChapter;
import com.example.audiobook_for_kids.repository.AudioRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.model.FavoriteBook;

import java.util.ArrayList;
import java.util.List;

// Added Glide import
import com.bumptech.glide.Glide;

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

    // repositories & adapter
    private AudioRepository audioRepository;
    private ChapterAdapter chapterAdapter;
    private java.util.List<AudioChapter> currentChapters = new java.util.ArrayList<>();

    // User activity repo
    private UserActivityRepository activityRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_detail);

        // Ánh xạ views
        initViews();

        // Nhận dữ liệu từ Intent
        loadDataFromIntent();

        // Setup chapter list UI
        setupChapterList();

        // Setup activity repo
        activityRepo = UserActivityRepository.getInstance(this);
        activityRepo.getError().observe(this, msg -> { if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); });
        activityRepo.getFavoritesLive().observe(this, favs -> {
            // update favorite state if current book in favorites
            if (favs != null && currentBookId != null) {
                boolean found = false;
                for (FavoriteBook fb : favs) {
                    if (fb.getBookId() != null && fb.getBookId().equals(currentBookId)) {
                        found = true;
                        break;
                    }
                }
                setFavoriteUI(found, false);
            }
        });

        // Try to fetch favorites (if logged in)
        activityRepo.fetchFavorites();

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

    private String currentBookId = null;
    private String currentBookTitle = null;
    private String currentBookAuthor = null;
    private String currentBookCover = null;

    private void loadDataFromIntent() {
        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy dữ liệu sách từ intent
            String bookTitle = intent.getStringExtra("book_title");
            String bookAuthor = intent.getStringExtra("book_author");
            String bookCover = intent.getStringExtra("book_cover");
            String bookDescription = intent.getStringExtra("book_description");
            String bookId = intent.getStringExtra("book_id");

            currentBookId = bookId;
            currentBookTitle = bookTitle;
            currentBookAuthor = bookAuthor;
            currentBookCover = bookCover;

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

            // Load cover image if available using Glide
            if (currentBookCover != null && !currentBookCover.isEmpty()) {
                Glide.with(this)
                        .load(currentBookCover)
                        .placeholder(R.color.teal_200)
                        .error(R.color.black)
                        .centerCrop()
                        .into(ivCover);
            }
        }

        // Hiển thị thông tin mặc định khác
        tvDuration.setText("12 phút");
        tvRating.setText("4.8 ⭐");
    }

    private void setupChapterList() {
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new ChapterAdapter(this, new ArrayList<>(), chapter -> {
            // On chapter click -> open PlayerActivity and pass audio url + metadata
            Intent intent = new Intent(AudiobookDetailActivity.this, PlayerActivity.class);
            intent.putExtra("book_title", currentBookTitle);
            intent.putExtra("book_author", currentBookAuthor);
            intent.putExtra("book_cover", currentBookCover);
            intent.putExtra("audio_url", chapter.getAudioUrl());
            intent.putExtra("book_id", currentBookId);
            startActivity(intent);
        });
        rvEpisodes.setAdapter(chapterAdapter);

        audioRepository = AudioRepository.getInstance();
        audioRepository.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        audioRepository.getChaptersLiveData().observe(this, chapters -> {
            if (chapters != null) {
                currentChapters = chapters;
                chapterAdapter.setChapters(chapters);
            }
        });

        // Trigger load if we have book id
        if (currentBookId != null && !currentBookId.isEmpty()) {
            audioRepository.fetchChapters(currentBookId);
        }
    }

    private void setupClickListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút yêu thích
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Nút phát truyện
        btnPlay.setOnClickListener(v -> playAudiobook());

        // Gửi đánh giá khi nhấn vào tvRating
        tvRating.setOnClickListener(v -> showRatingDialog());
    }

    private void showRatingDialog() {
        if (currentBookId == null) {
            Toast.makeText(this, "Không có sách để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] options = {"1 ⭐", "2 ⭐", "3 ⭐", "4 ⭐", "5 ⭐"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đánh giá sách")
                .setSingleChoiceItems(options, -1, (dialog, which) -> {
                    int rating = which + 1;
                    // Send review with empty text for now
                    activityRepo.submitReview(currentBookId, rating, "", new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                runOnUiThread(() -> Toast.makeText(AudiobookDetailActivity.this, "Cảm ơn bạn đã đánh giá", Toast.LENGTH_SHORT).show());
                                dialog.dismiss();
                            } else {
                                runOnUiThread(() -> Toast.makeText(AudiobookDetailActivity.this, "Không thể gửi đánh giá: " + response.code(), Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            runOnUiThread(() -> Toast.makeText(AudiobookDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .show();
    }

    private void setFavoriteUI(boolean fav, boolean showToast) {
        isFavorite = fav;
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            if (showToast) Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            if (showToast) Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        boolean newState = !isFavorite;
        // Optimistic UI update
        setFavoriteUI(newState, true);

        if (currentBookId == null) return;

        activityRepo.setFavorite(currentBookId, newState, new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (!response.isSuccessful()) {
                    // revert
                    setFavoriteUI(!newState, true);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                setFavoriteUI(!newState, true);
            }
        });
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

            // Nếu đã tải danh sách chương, truyền audio_url của chương đầu tiên để tự động phát
            if (currentChapters != null && !currentChapters.isEmpty()) {
                intent.putExtra("audio_url", currentChapters.get(0).getAudioUrl());
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
