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

    private SharedPreferences prefs;
    private static final String PREF_NAME = "AudiobookPrefs";
    private static final String KEY_RECENT = "recent_books"; // stored as comma-separated ids

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Ánh xạ views
        tabLayout = findViewById(R.id.tab_layout);
        rvLibrary = findViewById(R.id.rv_library);
        emptyState = findViewById(R.id.empty_state);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập TabLayout
        setupTabs();

        // Xử lý Bottom Navigation
        setupBottomNavigation();

        // Setup mini player
        setupMiniPlayer();

        // Repos
        bookRepository = BookRepository.getInstance();
        activityRepo = UserActivityRepository.getInstance(this);

        // Observe book list changes
        bookRepository.getBooksLiveData().observe(this, books -> {
            // refresh current tab content
            int pos = tabLayout.getSelectedTabPosition();
            loadLibraryData(pos);
        });

        // Observe favorites updates
        activityRepo.getFavoritesLive().observe(this, favs -> {
            // if currently on favorites tab, refresh
            if (tabLayout.getSelectedTabPosition() == 2) loadLibraryData(2);
        });

        activityRepo.getError().observe(this, err -> {
            if (err != null) Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });

        // Trigger initial loads
        bookRepository.fetchBooks();
        activityRepo.fetchFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo tab Library được chọn
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);

        // refresh current tab
        int pos = tabLayout.getSelectedTabPosition();
        loadLibraryData(pos);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvLibrary.setLayoutManager(layoutManager);

        libraryAdapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            // open detail
            openBookDetail(book);
        });
        rvLibrary.setAdapter(libraryAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đã lưu"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã nghe"));
        tabLayout.addTab(tabLayout.newTab().setText("Yêu thích"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadLibraryData(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadLibraryData(int tabPosition) {
        List<Book> allBooks = bookRepository.getBooksLiveData().getValue();
        if (allBooks == null) allBooks = new ArrayList<>();

        List<Book> result = new ArrayList<>();

        switch (tabPosition) {
            case 0: // Saved - placeholder: currently empty
                result.clear();
                break;
            case 1: // Recent (Đã nghe)
                List<String> recentIds = getRecentIdsFromPrefs();
                // preserve order of recentIds
                for (String id : recentIds) {
                    for (Book b : allBooks) {
                        if (b.getId() != null && b.getId().equals(id)) {
                            result.add(b);
                            break;
                        }
                    }
                }
                break;
            case 2: // Favorites
                List<FavoriteBook> favs = activityRepo.getFavoritesLive().getValue();
                if (favs != null) {
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

        // Update UI
        if (result.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
            libraryAdapter.setBooks(result);
        }
    }

    private List<String> getRecentIdsFromPrefs() {
        String raw = prefs.getString(KEY_RECENT, "");
        List<String> ids = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return ids;
        String[] parts = raw.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) ids.add(t);
        }
        return ids;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(LibraryActivity.this, AIStoryActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(LibraryActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // Đã ở trang Thư viện
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(LibraryActivity.this, AccountActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void openBookDetail(Book book) {
        Intent intent = new Intent(this, AudiobookDetailActivity.class);
        intent.putExtra("book_id", book.getId());
        intent.putExtra("book_title", book.getTitle());
        intent.putExtra("book_author", book.getAuthor());
        intent.putExtra("book_cover", book.getCoverUrl());
        startActivity(intent);
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
