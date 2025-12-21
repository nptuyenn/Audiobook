package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;

import com.example.audiobook_for_kids.adapter.TopicAudiobookAdapter;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.repository.BookRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class TopicActivity extends AppCompatActivity {

    public static final String EXTRA_TOPIC_TYPE = "EXTRA_TOPIC_TYPE";
    public static final String EXTRA_TOPIC_TITLE = "EXTRA_TOPIC_TITLE";

    // Standardized Constants to match Backend EXACTLY
    public static final String TOPIC_CO_TICH = "Cổ tích";
    public static final String TOPIC_NUOC_NGOAI = "Nước ngoài";
    public static final String TOPIC_NGU_NGON = "Ngụ ngôn";
    public static final String TOPIC_GIAO_DUC = "Giáo dục";
    public static final String TOPIC_PHIEU_LUU = "Phiêu lưu";
    public static final String TOPIC_GIA_DINH = "Gia đình";
    public static final String TOPIC_KY_NANG_SONG = "Kỹ năng sống";
    public static final String TOPIC_TINH_BAN = "Tình bạn";
    public static final String TOPIC_KHOA_HOC = "Khoa học";
    public static final String TOPIC_TRUONG_LOP = "Trường lớp";

    private TextView tvTopicTitle, tvTopicDescription;
    private RecyclerView rvStories;
    private LinearLayout layoutEmpty;
    private ImageView btnBack;

    private TopicAudiobookAdapter adapter;
    private List<Book> filteredStories = new ArrayList<>();
    private String currentTopicType;

    // Mini player views
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        currentTopicType = getIntent().getStringExtra(EXTRA_TOPIC_TYPE);
        String topicTitle = getIntent().getStringExtra(EXTRA_TOPIC_TITLE);

        initViews();
        setupTopicData(currentTopicType, topicTitle);
        setupRecyclerView();
        setupListeners();
        
        // Luôn chủ động fetch mới để đảm bảo có data mới nhất từ server
        BookRepository.getInstance().fetchBooks();
        
        BookRepository.getInstance().getBooksLiveData().observe(this, books -> {
            if (books != null) {
                filterBooksByCategory(books);
            }
        });

        setupBottomNavigation();
        setupMiniPlayer();
    }

    private void initViews() {
        tvTopicTitle = findViewById(R.id.tv_topic_title);
        tvTopicDescription = findViewById(R.id.tv_topic_description);
        rvStories = findViewById(R.id.rv_stories);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupTopicData(String topicType, String topicTitle) {
        tvTopicTitle.setText(topicTitle);
        String description;
        if (topicType == null) description = "Khám phá thế giới truyện hay";
        else {
            switch (topicType) {
                case TOPIC_CO_TICH: description = "Thế giới cổ tích kỳ diệu"; break;
                case TOPIC_NUOC_NGOAI: description = "Truyện nước ngoài đặc sắc"; break;
                case TOPIC_NGU_NGON: description = "Bài học ngụ ngôn sâu sắc"; break;
                case TOPIC_GIAO_DUC: description = "Kiến thức giáo dục bổ ích"; break;
                case TOPIC_PHIEU_LUU: description = "Cuộc phiêu lưu kỳ thú"; break;
                case TOPIC_GIA_DINH: description = "Tình cảm gia đình ấm áp"; break;
                case TOPIC_KY_NANG_SONG: description = "Rèn luyện kỹ năng cho bé"; break;
                case TOPIC_TINH_BAN: description = "Những người bạn tuyệt vời"; break;
                case TOPIC_KHOA_HOC: description = "Khám phá thế giới quanh ta"; break;
                case TOPIC_TRUONG_LOP: description = "Niềm vui khi đến trường"; break;
                default: description = "Khám phá thế giới truyện hay";
            }
        }
        tvTopicDescription.setText(description);
    }

    private void filterBooksByCategory(List<Book> allBooks) {
        filteredStories.clear();
        for (Book b : allBooks) {
            if (currentTopicType != null && b.getCategory() != null) {
                // So sánh CHUẨN: trimmed, ignore case, và kiểm tra chứa chuỗi
                String cat = b.getCategory().trim();
                String target = currentTopicType.trim();
                
                if (cat.equalsIgnoreCase(target) || cat.contains(target) || target.contains(cat)) {
                    filteredStories.add(b);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        if (filteredStories.isEmpty()) {
            rvStories.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvStories.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new TopicAudiobookAdapter(filteredStories, item -> {
            Intent intent = new Intent(this, AudiobookDetailActivity.class);
            intent.putExtra("book_id", item.getId());
            intent.putExtra("book_title", item.getTitle());
            intent.putExtra("book_author", item.getAuthor());
            intent.putExtra("book_cover", item.getCoverUrl());
            intent.putExtra("book_description", item.getDescription());
            intent.putExtra("book_rating", item.getAvgRating());
            startActivity(intent);
        });
        rvStories.setLayoutManager(new GridLayoutManager(this, 2));
        rvStories.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
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
