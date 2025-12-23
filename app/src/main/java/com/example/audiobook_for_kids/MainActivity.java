package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.audiobook_for_kids.adapter.AudiobookAdapter;
import com.example.audiobook_for_kids.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFeatured, rvSuggestions, rvQuickPicks, rvFamousAuthors;
    private AudiobookAdapter featuredAdapter, suggestionsAdapter, quickPicksAdapter, famousAuthorsAdapter;
    private BookRepository bookRepository;
    private UserActivityRepository activityRepo;
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    private static final String PREF_NAME = "AudiobookPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvFeatured = findViewById(R.id.rv_featured);
        rvSuggestions = findViewById(R.id.rv_suggestions);
        rvQuickPicks = findViewById(R.id.rv_quick_picks);
        rvFamousAuthors = findViewById(R.id.rv_famous_authors);

        setupFeaturedRecycler();
        setupSuggestionsRecycler();
        setupQuickPicksRecycler();
        setupFamousAuthorsRecycler();
        setupBottomNavigation();
        setupTopicClickListeners();

        Button btn_login = findViewById(R.id.btn_login);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLogged = prefs.getBoolean("is_logged_in", false);
        btn_login.setVisibility(isLogged ? Button.GONE : Button.VISIBLE);

        btn_login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        });

        LinearLayout search_bar = findViewById(R.id.search_bar);
        search_bar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        bookRepository = BookRepository.getInstance();
        bookRepository.getBooksLiveData().observe(this, books -> {
            if (books != null) {
                updateRecyclerData(books);
            }
        });

        setupMiniPlayer();

        activityRepo = UserActivityRepository.getInstance(this);
        activityRepo.getFavoritesLive().observe(this, favs -> {
            if (favs == null) return;
            markFavoritesInAdapter(favs);
        });

        // SỬA LỖI: Sử dụng getHistoryLive() thay vì getRecentListensLive()
        activityRepo.getHistoryLive().observe(this, recentBooks -> {
            if (recentBooks != null && !recentBooks.isEmpty()) {
                featuredAdapter.setBooks(recentBooks.stream().limit(5).collect(Collectors.toList()));
            }
        });

        bookRepository.fetchBooks();
        activityRepo.fetchFavorites();
        activityRepo.fetchHistory(); // SỬA LỖI: Gọi fetchHistory()
    }

    private void updateRecyclerData(List<Book> allBooks) {
        List<Book> trending = allBooks.stream()
                .sorted((b1, b2) -> Float.compare(b2.getAvgRating(), b1.getAvgRating()))
                .limit(5)
                .collect(Collectors.toList());
        suggestionsAdapter.setBooks(trending);

        List<Book> quickPicks = allBooks.stream()
                .filter(b -> b.getCategory() != null && (b.getCategory().contains("Giáo dục") || b.getCategory().contains("Cổ tích")))
                .limit(5)
                .collect(Collectors.toList());
        quickPicksAdapter.setBooks(quickPicks);

        List<Book> byAuthors = allBooks.stream()
                .filter(b -> b.getAuthor() != null && !b.getAuthor().isEmpty())
                .limit(5)
                .collect(Collectors.toList());
        famousAuthorsAdapter.setBooks(byAuthors);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Button btn_login = findViewById(R.id.btn_login);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLogged = prefs.getBoolean("is_logged_in", false);
        btn_login.setVisibility(isLogged ? Button.GONE : Button.VISIBLE);
        
        if (activityRepo != null) {
            activityRepo.fetchFavorites();
            activityRepo.fetchHistory(); // SỬA LỖI: Làm mới lịch sử
        }
    }

    private void markFavoritesInAdapter(List<FavoriteBook> favs) {
        java.util.Set<String> favIds = favs.stream().map(FavoriteBook::getBookId).collect(Collectors.toSet());
        featuredAdapter.updateFavorites(favIds);
        suggestionsAdapter.updateFavorites(favIds);
        quickPicksAdapter.updateFavorites(favIds);
        famousAuthorsAdapter.updateFavorites(favIds);
    }

    private void setupFeaturedRecycler() {
        rvFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter = new AudiobookAdapter(this, new ArrayList<>(), this::openBookDetail);
        rvFeatured.setAdapter(featuredAdapter);
    }

    private void setupSuggestionsRecycler() {
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        suggestionsAdapter = new AudiobookAdapter(this, new ArrayList<>(), this::openBookDetail);
        rvSuggestions.setAdapter(suggestionsAdapter);
    }

    private void setupQuickPicksRecycler() {
        rvQuickPicks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quickPicksAdapter = new AudiobookAdapter(this, new ArrayList<>(), this::openBookDetail);
        rvQuickPicks.setAdapter(quickPicksAdapter);
    }

    private void setupFamousAuthorsRecycler() {
        rvFamousAuthors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        famousAuthorsAdapter = new AudiobookAdapter(this, new ArrayList<>(), this::openBookDetail);
        rvFamousAuthors.setAdapter(famousAuthorsAdapter);
    }

    private void openBookDetail(Book book) {
        Intent intent = new Intent(this, AudiobookDetailActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_cover", book.getCoverUrl());
        intent.putExtra("book_description", book.getDescription());
        intent.putExtra("book_rating", book.getAvgRating());
        intent.putExtra("is_ai", book.isAi());
        intent.putExtra("audio_url", book.getAudioUrl());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_ai) {
                startActivity(new Intent(this, AIStoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_library) {
                startActivity(new Intent(this, LibraryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return itemId == R.id.nav_home;
        });
    }

    private void setupTopicClickListeners() {
        findViewById(R.id.topic_co_tich).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));
        findViewById(R.id.topic_nuoc_ngoai).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));
        findViewById(R.id.topic_ngu_ngon).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));
        findViewById(R.id.topic_giao_duc).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));
        findViewById(R.id.topic_phieu_luu).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        Intent intent = new Intent(this, TopicActivity.class);
        intent.putExtra(TopicActivity.EXTRA_TOPIC_TYPE, topicType);
        intent.putExtra(TopicActivity.EXTRA_TOPIC_TITLE, topicTitle);
        startActivity(intent);
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
                intent.putExtra("from_mini_player", true);
                startActivity(intent);
            }
        });
    }
}
