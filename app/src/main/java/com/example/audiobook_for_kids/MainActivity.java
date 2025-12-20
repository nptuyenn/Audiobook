package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
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

import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import androidx.lifecycle.Observer;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFeatured;
    private RecyclerView rvSuggestions;
    private RecyclerView rvQuickPicks;
    private RecyclerView rvFamousAuthors;

    // Adapters
    private AudiobookAdapter featuredAdapter;
    private AudiobookAdapter suggestionsAdapter;
    private AudiobookAdapter quickPicksAdapter;
    private AudiobookAdapter famousAuthorsAdapter;

    private BookRepository bookRepository;
    private UserActivityRepository activityRepo;

    // Mini player views
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

        // hide login button when already logged in
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLogged = prefs.getBoolean("is_logged_in", false);
        btn_login.setVisibility(isLogged ? Button.GONE : Button.VISIBLE);

        btn_login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            // Hiệu ứng: LoginActivity trượt lên từ dưới, MainActivity giữ nguyên (smooth version)
            overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
        });

        LinearLayout search_bar = findViewById(R.id.search_bar);
        search_bar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Initialize repository and observe data
        bookRepository = BookRepository.getInstance();
        bookRepository.getLoading().observe(this, isLoading -> {
            // TODO: show/hide a ProgressBar if you add one to layout
        });
        bookRepository.getError().observe(this, errorMsg -> {
            if (errorMsg != null) Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        });
        bookRepository.getBooksLiveData().observe(this, books -> {
            if (books != null) {
                // For now, populate all lists with same data. Later you can filter by category.
                featuredAdapter.setBooks(books);
                suggestionsAdapter.setBooks(books);
                quickPicksAdapter.setBooks(books);
                famousAuthorsAdapter.setBooks(books);
            }
        });

        // Setup mini player
        setupMiniPlayer();

        // Setup activity repo to sync favorite badges
        activityRepo = UserActivityRepository.getInstance(this);
        activityRepo.getFavoritesLive().observe(this, favs -> {
            if (favs == null) return;
            // When favorites arrive, mark books in current adapters
            markFavoritesInAdapter(favs);
        });

        activityRepo.getError().observe(this, err -> {
            // silent for unauthenticated; optional show
            // if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        // Trigger initial load
        bookRepository.fetchBooks();
        // Try to fetch favorites (if logged in)
        activityRepo.fetchFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update login button visibility when returning to activity
        Button btn_login = findViewById(R.id.btn_login);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isLogged = prefs.getBoolean("is_logged_in", false);
        btn_login.setVisibility(isLogged ? Button.GONE : Button.VISIBLE);

        // refresh favorites
        activityRepo.fetchFavorites();
    }

    private void markFavoritesInAdapter(List<FavoriteBook> favs) {
        // Helper to mark by id
        java.util.Set<String> favIds = new java.util.HashSet<>();
        for (FavoriteBook fb : favs) if (fb.getBookId() != null) favIds.add(fb.getBookId());

        // update lists if they have data
        updateBooksWithFavorites(featuredAdapter, favIds);
        updateBooksWithFavorites(suggestionsAdapter, favIds);
        updateBooksWithFavorites(quickPicksAdapter, favIds);
        updateBooksWithFavorites(famousAuthorsAdapter, favIds);
    }

    private void updateBooksWithFavorites(AudiobookAdapter adapter, java.util.Set<String> favIds) {
        try {
            // Reflection: adapter holds List<Book> as a private field; instead, re-fetch from repository and update
            List<Book> books = bookRepository.getBooksLiveData().getValue();
            if (books == null) return;
            for (Book b : books) {
                b.setFavorite(b.getId() != null && favIds.contains(b.getId()));
            }
            adapter.setBooks(books);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFeaturedRecycler() {
        // Cấu hình LayoutManager (cuộn ngang)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFeatured.setLayoutManager(layoutManager);

        // Start with empty list; will be filled when API returns
        featuredAdapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            // Xử lý khi click vào sách
            openBookDetail(book);
        });
        rvFeatured.setAdapter(featuredAdapter);
    }

    private void setupSuggestionsRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSuggestions.setLayoutManager(layoutManager);

        // Start empty
        suggestionsAdapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            // Xử lý khi click vào sách
            openBookDetail(book);
        });
        rvSuggestions.setAdapter(suggestionsAdapter);
    }


    private void setupQuickPicksRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvQuickPicks.setLayoutManager(layoutManager);

        quickPicksAdapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            openBookDetail(book);
        });
        rvQuickPicks.setAdapter(quickPicksAdapter);
    }


    private void setupFamousAuthorsRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFamousAuthors.setLayoutManager(layoutManager);

        famousAuthorsAdapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            openBookDetail(book);
        });
        rvFamousAuthors.setAdapter(famousAuthorsAdapter);
    }


    // TODO: Sau này thêm các method  để load dữ liệu

    private void openBookDetail(Book book) {
        try {
            Intent intent = new Intent(this, AudiobookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("book_title", book.getTitle());
            intent.putExtra("book_author", book.getAuthor());
            intent.putExtra("book_cover", book.getCoverUrl());
            intent.putExtra("book_description", book.getDescription());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở chi tiết sách", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(MainActivity.this, AIStoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            }
            else if (itemId == R.id.nav_library) {
                Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void setupTopicClickListeners() {
        findViewById(R.id.topic_co_tich).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));

        findViewById(R.id.topic_nuoc_ngoai).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));

        findViewById(R.id.topic_ngu_ngon).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));

        findViewById(R.id.topic_giao_duc).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));

        findViewById(R.id.topic_phieu_luu).setOnClickListener(v ->
                openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        try {
            Intent intent = new Intent(this, TopicActivity.class);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TYPE, topicType);
            intent.putExtra(TopicActivity.EXTRA_TOPIC_TITLE, topicTitle);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở chủ đề", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMiniPlayer() {
        // Initialize views
        layoutMiniPlayer = findViewById(R.id.layout_mini_player);
        ivMiniCover = findViewById(R.id.iv_mini_cover);
        tvMiniTitle = findViewById(R.id.tv_mini_title);
        tvMiniAuthor = findViewById(R.id.tv_mini_author);
        btnMiniPlay = findViewById(R.id.btn_mini_play);

        // Initialize audio manager
        audioManager = AudioPlaybackManager.getInstance();
        audioManager.initialize(this);

        // Observe audio manager state
        audioManager.getShouldShowMiniPlayer().observe(this, shouldShow -> {
            if (shouldShow != null) {
                layoutMiniPlayer.setVisibility(shouldShow ? CardView.VISIBLE : CardView.GONE);
            }
        });

        audioManager.getCurrentTitle().observe(this, title -> {
            if (title != null) {
                tvMiniTitle.setText(title);
            }
        });

        audioManager.getCurrentAuthor().observe(this, author -> {
            if (author != null) {
                tvMiniAuthor.setText(author);
            }
        });

        audioManager.getCurrentCover().observe(this, coverUrl -> {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this)
                        .load(coverUrl)
                        .placeholder(R.drawable.ic_headphone_placeholder)
                        .error(R.drawable.ic_headphone_placeholder)
                        .into(ivMiniCover);
            }
        });

        audioManager.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) {
                btnMiniPlay.setImageResource(isPlaying ?
                    R.drawable.ic_pause : R.drawable.ic_play_arrow);
            }
        });

        // Set click listeners
        btnMiniPlay.setOnClickListener(v -> {
            Boolean playing = audioManager.getIsPlaying().getValue();
            if (playing != null && playing) {
                audioManager.pause();
            } else {
                audioManager.play();
            }
        });

        layoutMiniPlayer.setOnClickListener(v -> {
            // Open PlayerActivity with current playing book
            String bookId = audioManager.getCurrentBookId().getValue();
            String title = audioManager.getCurrentTitle().getValue();
            String author = audioManager.getCurrentAuthor().getValue();
            String cover = audioManager.getCurrentCover().getValue();

            if (bookId != null && !bookId.isEmpty()) {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("book_id", bookId);
                intent.putExtra("book_title", title);
                intent.putExtra("book_author", author);
                intent.putExtra("book_cover", cover);
                intent.putExtra("from_mini_player", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.no_animation);
            }
        });
    }
}