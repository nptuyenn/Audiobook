package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.button.MaterialButton;

import com.example.audiobook_for_kids.adapter.ChapterAdapter;
import com.example.audiobook_for_kids.model.AudioChapter;
import com.example.audiobook_for_kids.repository.AudioRepository;
import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

public class AudiobookDetailActivity extends AppCompatActivity {

    private ImageView ivCover, btnBack, btnFavorite;
    private TextView tvTitle, tvAuthor, tvRating, tvDescription, tvLabelChapters;
    private MaterialButton btnPlay;
    private RecyclerView rvEpisodes;

    private boolean isFavorite = false;
    private boolean isAiStory = false; 
    private String currentBookId, currentBookTitle, currentBookAuthor, currentBookCover, currentAudioUrl;
    
    private AudioRepository audioRepository;
    private ChapterAdapter chapterAdapter;
    private List<AudioChapter> currentChapters = new ArrayList<>();
    private UserActivityRepository activityRepo;

    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_detail);

        initViews();
        loadDataFromIntent();
        setupLogicByStoryType();
        setupMiniPlayer();
        activityRepo = UserActivityRepository.getInstance(this);
        
        activityRepo.getFavoritesLive().observe(this, favs -> {
            if (favs != null && currentBookId != null) {
                boolean found = favs.stream().anyMatch(fb -> currentBookId.equals(fb.getBookId()));
                setFavoriteUI(found, false);
            }
        });

        BookRepository.getInstance().getBooksLiveData().observe(this, books -> {
            if (books != null && currentBookId != null) {
                for (Book b : books) {
                    if (b.getId().equals(currentBookId)) {
                        tvRating.setText(String.format("%.1f ⭐", b.getAvgRating()));
                        break;
                    }
                }
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
        tvRating = findViewById(R.id.tv_rating);
        tvDescription = findViewById(R.id.tv_description);
        btnPlay = findViewById(R.id.btn_play);
        rvEpisodes = findViewById(R.id.rv_episodes);
        tvLabelChapters = findViewById(R.id.tv_label_chapters);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            isAiStory = intent.getBooleanExtra("is_ai", false);
            currentBookId = intent.getStringExtra("book_id");
            currentBookTitle = intent.getStringExtra("book_title");
            currentBookAuthor = intent.getStringExtra("book_author");
            currentBookCover = intent.getStringExtra("book_cover");
            currentAudioUrl = intent.getStringExtra("audio_url");
            float rating = intent.getFloatExtra("book_rating", 0f);

            tvTitle.setText(currentBookTitle != null ? currentBookTitle : "Unknown Title");
            tvAuthor.setText(currentBookAuthor != null ? "Tác giả: " + currentBookAuthor : "");
            
            if (isAiStory) {
                tvDescription.setText("Truyện do AI tạo");
            } else {
                String desc = intent.getStringExtra("book_description");
                tvDescription.setText(desc != null ? desc : "");
            }
            
            tvRating.setText(String.format("%.1f ⭐", rating));
            
            String cover = (currentBookCover == null || currentBookCover.isEmpty()) 
                    ? "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766407370/AI_cover_b69ymc.jpg" 
                    : currentBookCover;
                    
            Glide.with(this).load(cover).placeholder(R.drawable.ic_headphone_placeholder).into(ivCover);
        }
    }

    private void setupLogicByStoryType() {
        if (isAiStory) {
            rvEpisodes.setVisibility(View.GONE);
            tvLabelChapters.setVisibility(View.GONE);
        } else {
            rvEpisodes.setVisibility(View.VISIBLE);
            tvLabelChapters.setVisibility(View.VISIBLE);

            rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
            chapterAdapter = new ChapterAdapter(this, new ArrayList<>(), chapter -> {
                activityRepo.updateProgress(currentBookId, chapter.getChapter(), 0);
                startPlayback(chapter.getAudioUrl());
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
    }

    private void startPlayback(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy nội dung âm thanh", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("book_id", currentBookId);
        intent.putExtra("book_title", currentBookTitle);
        intent.putExtra("book_author", currentBookAuthor);
        intent.putExtra("book_cover", currentBookCover);
        intent.putExtra("audio_url", url);
        // TRUYỀN FLAG SANG PLAYER ĐỂ CHẶN 404
        intent.putExtra("is_ai", isAiStory); 
        startActivity(intent);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        
        findViewById(R.id.btn_open_rating).setOnClickListener(v -> showRatingDialog());

        btnPlay.setOnClickListener(v -> {
            if (isAiStory && currentAudioUrl != null) {
                activityRepo.updateProgress(currentBookId, 1, 0);
                startPlayback(currentAudioUrl);
            } else if (!currentChapters.isEmpty()) {
                activityRepo.updateProgress(currentBookId, currentChapters.get(0).getChapter(), 0);
                startPlayback(currentChapters.get(0).getAudioUrl());
            } else {
                Toast.makeText(this, "Nội dung chưa sẵn sàng, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRatingDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rating_star, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_custom);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int ratingValue = (int) ratingBar.getRating();
            if (ratingValue == 0) {
                Toast.makeText(this, "Bé hãy chọn số sao nhé!", Toast.LENGTH_SHORT).show();
                return;
            }

            activityRepo.submitReview(currentBookId, ratingValue, "", new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AudiobookDetailActivity.this, "Cảm ơn bé đã đánh giá truyện!", Toast.LENGTH_SHORT).show();
                        BookRepository.getInstance().fetchBooks();
                        dialog.dismiss();
                    }
                }
                @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(AudiobookDetailActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
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

    private void setupMiniPlayer() {
        layoutMiniPlayer = findViewById(R.id.layout_mini_player);
        ivMiniCover = findViewById(R.id.iv_mini_cover);
        tvMiniTitle = findViewById(R.id.tv_mini_title);
        tvMiniAuthor = findViewById(R.id.tv_mini_author);
        btnMiniPlay = findViewById(R.id.btn_mini_play);
        audioManager = AudioPlaybackManager.getInstance();
        audioManager.initialize(this);
        audioManager.getShouldShowMiniPlayer().observe(this, shouldShow -> {
            if (shouldShow != null) layoutMiniPlayer.setVisibility(shouldShow ? CardView.VISIBLE : CardView.GONE);
        });
        audioManager.getCurrentTitle().observe(this, title -> { if (title != null) tvMiniTitle.setText(title); });
        audioManager.getCurrentAuthor().observe(this, author -> { if (author != null) tvMiniAuthor.setText(author); });
        audioManager.getCurrentCover().observe(this, coverUrl -> {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this).load(coverUrl).placeholder(R.drawable.ic_headphone_placeholder).into(ivMiniCover);
            }
        });
        audioManager.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) btnMiniPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        });
        btnMiniPlay.setOnClickListener(v -> {
            Boolean playing = audioManager.getIsPlaying().getValue();
            if (playing != null && playing) audioManager.pause(); else audioManager.play();
        });
        layoutMiniPlayer.setOnClickListener(v -> {
            String bookId = audioManager.getCurrentBookId().getValue();
            if (bookId != null && !bookId.isEmpty()) {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("book_id", bookId);
                intent.putExtra("book_title", audioManager.getCurrentTitle().getValue());
                intent.putExtra("book_author", audioManager.getCurrentAuthor().getValue());
                intent.putExtra("book_cover", audioManager.getCurrentCover().getValue());
                intent.putExtra("from_mini_player", true);
                startActivity(intent);
            }
        });
    }
}
