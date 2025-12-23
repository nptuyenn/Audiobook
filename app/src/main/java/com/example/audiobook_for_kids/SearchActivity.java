package com.example.audiobook_for_kids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.audiobook_for_kids.adapter.AudiobookAdapter;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.repository.BookRepository;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView ivClearSearch;
    private RecyclerView rvResults;
    private AudiobookAdapter adapter;
    private List<Book> allBooks = new ArrayList<>();
    private TextView tvRecentSearch, tvSearchSuggest;
    private RecyclerView rvRecentSearches; // Reuse rv_topic_cards for recent search text
    private View layoutTopicCardsRoot;

    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    private static final String PREFS_NAME = "SearchPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupTopicClickListeners();
        loadRecentSearches();

        BookRepository.getInstance().getBooksLiveData().observe(this, books -> {
            if (books != null) {
                allBooks = books;
            }
        });
        BookRepository.getInstance().fetchBooks();
        setupMiniPlayer();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        ivClearSearch = findViewById(R.id.iv_clear_search);
        rvResults = findViewById(R.id.rv_search_results);
        tvRecentSearch = findViewById(R.id.recent_search);
        tvSearchSuggest = findViewById(R.id.search_suggest);
        rvRecentSearches = findViewById(R.id.rv_topic_cards);
        layoutTopicCardsRoot = findViewById(R.id.card_cotich).getParent().getParent() instanceof View ? (View)findViewById(R.id.card_cotich).getParent().getParent() : null;
        
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AudiobookAdapter(this, new ArrayList<>(), book -> {
            saveSearchQuery(book.getTitle());
            Intent intent = new Intent(this, AudiobookDetailActivity.class);
            intent.putExtra("book_id", book.getId());
            intent.putExtra("book_title", book.getTitle());
            intent.putExtra("book_author", book.getAuthor());
            intent.putExtra("book_cover", book.getCoverUrl());
            intent.putExtra("book_description", book.getDescription());
            intent.putExtra("book_rating", book.getAvgRating());
            startActivity(intent);
        });
        // Sửa thành 2 cột
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvResults.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showDefaultUI();
                } else {
                    performSearch(query);
                }
                ivClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                saveSearchQuery(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void performSearch(String query) {
        List<Book> results = allBooks.stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                             (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(query.toLowerCase())) ||
                             (b.getCategory() != null && b.getCategory().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());

        adapter.setBooks(results);
        rvResults.setVisibility(View.VISIBLE);
        tvRecentSearch.setVisibility(View.GONE);
        rvRecentSearches.setVisibility(View.GONE);
        tvSearchSuggest.setVisibility(View.GONE);
        if (layoutTopicCardsRoot != null) layoutTopicCardsRoot.setVisibility(View.GONE);
    }

    private void showDefaultUI() {
        rvResults.setVisibility(View.GONE);
        tvRecentSearch.setVisibility(View.VISIBLE);
        rvRecentSearches.setVisibility(View.VISIBLE);
        tvSearchSuggest.setVisibility(View.VISIBLE);
        if (layoutTopicCardsRoot != null) layoutTopicCardsRoot.setVisibility(View.VISIBLE);
        loadRecentSearches();
    }

    private void saveSearchQuery(String query) {
        if (query.isEmpty()) return;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_RECENT_SEARCHES, "");
        List<String> list = new ArrayList<>(Arrays.asList(saved.split(",")));
        list.remove(query);
        list.add(0, query);
        if (list.size() > 10) list = list.subList(0, 10);
        prefs.edit().putString(KEY_RECENT_SEARCHES, String.join(",", list)).apply();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_RECENT_SEARCHES, "");
        if (saved.isEmpty()) {
            tvRecentSearch.setVisibility(View.GONE);
            rvRecentSearches.setVisibility(View.GONE);
            return;
        }
        tvRecentSearch.setVisibility(View.VISIBLE);
        rvRecentSearches.setVisibility(View.VISIBLE);
        // Using a simple text adapter for recent searches could be done here, 
        // but for now I'll just show them or let the user know they are saved.
    }

    private void setupTopicClickListeners() {
        findViewById(R.id.card_cotich).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_CO_TICH, "Cổ tích"));
        findViewById(R.id.card_nuoc_ngoai).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_NUOC_NGOAI, "Nước ngoài"));
        findViewById(R.id.card_ngu_ngon).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_NGU_NGON, "Ngụ ngôn"));
        findViewById(R.id.card_giao_duc).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_GIAO_DUC, "Giáo dục"));
        findViewById(R.id.card_phieu_luu).setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_PHIEU_LUU, "Phiêu lưu"));
        
        View cardGiaDinh = findViewById(R.id.card_gia_dinh);
        if (cardGiaDinh != null) cardGiaDinh.setOnClickListener(v -> openTopicActivity(TopicActivity.TOPIC_GIA_DINH, "Gia đình"));
    }

    private void openTopicActivity(String topicType, String topicTitle) {
        saveSearchQuery(topicTitle);
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
                intent.putExtra("book_title", audioManager.getCurrentTitle().getValue());
                intent.putExtra("book_author", audioManager.getCurrentAuthor().getValue());
                intent.putExtra("book_cover", audioManager.getCurrentCover().getValue());
                intent.putExtra("from_mini_player", true);
                startActivity(intent);
            }
        });
    }
}
