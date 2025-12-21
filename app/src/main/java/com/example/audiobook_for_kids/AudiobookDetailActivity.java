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
import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.model.FavoriteBook;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

public class AudiobookDetailActivity extends AppCompatActivity {

    private ImageView ivCover, btnBack, btnFavorite;
    private TextView tvTitle, tvAuthor, tvDuration, tvRating, tvDescription;
    private MaterialButton btnPlay;
    private RecyclerView rvEpisodes;

    private boolean isFavorite = false;
    private String currentBookId, currentBookTitle, currentBookAuthor, currentBookCover;
    
    private AudioRepository audioRepository;
    private ChapterAdapter chapterAdapter;
    private List<AudioChapter> currentChapters = new ArrayList<>();
    private UserActivityRepository activityRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_detail);

        initViews();
        loadDataFromIntent();
        setupChapterList();

        activityRepo = UserActivityRepository.getInstance(this);
        activityRepo.getFavoritesLive().observe(this, favs -> {
            if (favs != null && currentBookId != null) {
                boolean found = favs.stream().anyMatch(fb -> currentBookId.equals(fb.getBookId()));
                setFavoriteUI(found, false);
            }
        });

        activityRepo.fetchFavorites();
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
        Intent intent = getIntent();
        if (intent != null) {
            currentBookId = intent.getStringExtra("book_id");
            currentBookTitle = intent.getStringExtra("book_title");
            currentBookAuthor = intent.getStringExtra("book_author");
            currentBookCover = intent.getStringExtra("book_cover");
            String desc = intent.getStringExtra("book_description");
            float rating = intent.getFloatExtra("book_rating", 0f);

            tvTitle.setText(currentBookTitle != null ? currentBookTitle : "Unknown Title");
            tvAuthor.setText(currentBookAuthor != null ? "Tác giả: " + currentBookAuthor : "");
            tvDescription.setText(desc != null ? desc : "");
            tvRating.setText(String.format("%.1f ⭐", rating));

            Glide.with(this).load(currentBookCover).placeholder(R.drawable.ic_headphone_placeholder).into(ivCover);
        }
    }

    private void setupChapterList() {
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        chapterAdapter = new ChapterAdapter(this, new ArrayList<>(), chapter -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            // TRUYỀN ĐẦY ĐỦ THÔNG TIN
            intent.putExtra("book_id", currentBookId);
            intent.putExtra("book_title", currentBookTitle);
            intent.putExtra("book_author", currentBookAuthor);
            intent.putExtra("book_cover", currentBookCover);
            intent.putExtra("audio_url", chapter.getAudioUrl());
            startActivity(intent);
        });
        rvEpisodes.setAdapter(chapterAdapter);

        audioRepository = AudioRepository.getInstance();
        audioRepository.getChaptersLiveData().observe(this, chapters -> {
            if (chapters != null) {
                currentChapters = chapters;
                chapterAdapter.setChapters(chapters);
            }
        });

        if (currentBookId != null) audioRepository.fetchChapters(currentBookId);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        tvRating.setOnClickListener(v -> showRatingDialog());
        btnPlay.setOnClickListener(v -> {
            if (!currentChapters.isEmpty()) {
                Intent intent = new Intent(this, PlayerActivity.class);
                // TRUYỀN ĐẦY ĐỦ THÔNG TIN KHI NHẤN NÚT PHÁT
                intent.putExtra("book_id", currentBookId);
                intent.putExtra("book_title", currentBookTitle);
                intent.putExtra("book_author", currentBookAuthor);
                intent.putExtra("book_cover", currentBookCover);
                intent.putExtra("audio_url", currentChapters.get(0).getAudioUrl());
                intent.putExtra("auto_play", true);
                startActivity(intent);
            }
        });
    }

    private void showRatingDialog() {
        final String[] options = {"1 ⭐", "2 ⭐", "3 ⭐", "4 ⭐", "5 ⭐"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Đánh giá sách")
                .setItems(options, (dialog, which) -> {
                    int ratingValue = which + 1;
                    activityRepo.submitReview(currentBookId, ratingValue, "", new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AudiobookDetailActivity.this, "Cảm ơn bạn!", Toast.LENGTH_SHORT).show();
                                BookRepository.getInstance().fetchBooks();
                            }
                        }
                        @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {}
                    });
                }).show();
    }

    private void setFavoriteUI(boolean fav, boolean showToast) {
        isFavorite = fav;
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void toggleFavorite() {
        boolean newState = !isFavorite;
        setFavoriteUI(newState, true);
        activityRepo.setFavorite(currentBookId, newState, null);
    }
}
