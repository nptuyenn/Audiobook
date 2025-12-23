package com.example.audiobook_for_kids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import com.example.audiobook_for_kids.adapter.AudiobookAdapter;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.cardview.widget.CardView;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvLibrary;
    private LinearLayout emptyState;

    private AudiobookAdapter libraryAdapter;
    private BookRepository bookRepository;
    private UserActivityRepository activityRepo;

    // Mini player views
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        tabLayout = findViewById(R.id.tab_layout);
        rvLibrary = findViewById(R.id.rv_library);
        emptyState = findViewById(R.id.empty_state);

        setupRecyclerView();
        setupTabs();
        setupBottomNavigation();
        setupMiniPlayer();

        bookRepository = BookRepository.getInstance();
        activityRepo = UserActivityRepository.getInstance(this);

        // Lắng nghe thay đổi dữ liệu từ các LiveData
        bookRepository.getBooksLiveData().observe(this, books -> {
            loadLibraryData(tabLayout.getSelectedTabPosition());
        });

        activityRepo.getFavoritesLive().observe(this, favs -> {
            if (tabLayout.getSelectedTabPosition() == 2) loadLibraryData(2);
        });

        activityRepo.getAiStoriesLive().observe(this, aiStories -> {
            if (tabLayout.getSelectedTabPosition() == 0) loadLibraryData(0);
        });

        activityRepo.getHistoryLive().observe(this, history -> {
            if (tabLayout.getSelectedTabPosition() == 1) loadLibraryData(1);
        });

        activityRepo.getError().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        // Tải dữ liệu ban đầu
        bookRepository.fetchBooks();
        activityRepo.fetchFavorites();
        activityRepo.fetchAIStories();
        activityRepo.fetchHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);
        
        activityRepo.fetchAIStories();
        activityRepo.fetchFavorites();
        activityRepo.fetchHistory();
        
        loadLibraryData(tabLayout.getSelectedTabPosition());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvLibrary.setLayoutManager(layoutManager);
        libraryAdapter = new AudiobookAdapter(this, new ArrayList<>(), this::openBookDetail);
        rvLibrary.setAdapter(libraryAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đã tạo"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã nghe"));
        tabLayout.addTab(tabLayout.newTab().setText("Yêu thích"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) activityRepo.fetchAIStories();
                else if (position == 1) activityRepo.fetchHistory();
                else if (position == 2) activityRepo.fetchFavorites();
                loadLibraryData(position); 
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadLibraryData(int tabPosition) {
        List<Book> allBooks = bookRepository.getBooksLiveData().getValue();
        if (allBooks == null) allBooks = new ArrayList<>();

        List<Book> result = new ArrayList<>();

        switch (tabPosition) {
            case 0: // Đã tạo
                List<Book> aiStories = activityRepo.getAiStoriesLive().getValue();
                if (aiStories != null) result.addAll(aiStories);
                break;

            case 1: // Đã nghe - CẬP NHẬT: Merge Rating từ allBooks
                List<Book> history = activityRepo.getHistoryLive().getValue();
                if (history != null) {
                    for (Book h : history) {
                        // Tìm sách trong allBooks để lấy Rating chính xác (vì history thường thiếu rating)
                        for (Book b : allBooks) {
                            if (b.getId().equals(h.getId())) {
                                h.setAvgRating(b.getAvgRating());
                                break;
                            }
                        }
                        result.add(h);
                    }
                }
                break;

            case 2: // Yêu thích
                List<FavoriteBook> favs = activityRepo.getFavoritesLive().getValue();
                if (favs != null && !allBooks.isEmpty()) {
                    Set<String> favIds = new HashSet<>();
                    for (FavoriteBook fb : favs) if (fb.getBookId() != null) favIds.add(fb.getBookId());
                    for (Book b : allBooks) {
                        if (b.getId() != null && favIds.contains(b.getId())) {
                            b.setFavorite(true);
                            result.add(b);
                        }
                    }
                }
                break;
        }
        updateUI(result);
    }

    private void updateUI(List<Book> result) {
        if (result.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
            libraryAdapter.setBooks(result);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) { finish(); return true; }
            if (itemId == R.id.nav_ai) { startActivity(new Intent(this, AIStoryActivity.class)); finish(); return true; }
            if (itemId == R.id.nav_account) { startActivity(new Intent(this, AccountActivity.class)); finish(); return true; }
            return itemId == R.id.nav_library;
        });
    }

    private void openBookDetail(Book book) {
        Intent intent = new Intent(this, AudiobookDetailActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_cover", book.getCoverUrl());
        intent.putExtra("audio_url", book.getAudioUrl());
        intent.putExtra("is_ai", book.isAi());
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
                intent.putExtra("book_title", audioManager.getCurrentTitle().getValue());
                intent.putExtra("book_author", audioManager.getCurrentAuthor().getValue());
                intent.putExtra("book_cover", audioManager.getCurrentCover().getValue());
                intent.putExtra("from_mini_player", true);
                startActivity(intent);
            }
        });
    }
}
