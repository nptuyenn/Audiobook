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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/****
 * Activity hiển thị danh sách truyện theo từng chủ đề
 * Có thể tái sử dụng cho tất cả các topic khác nhau
// DATABASE INTEGRATION NOTES:
 - Thay thế dữ liệu tĩnh bằng API/Database calls
 - Thêm pagination cho danh sách dài
 */
public class TopicActivity extends AppCompatActivity {

    private static final String TAG = "TopicActivity";

    public static final String EXTRA_TOPIC_TYPE = "EXTRA_TOPIC_TYPE";
    public static final String EXTRA_TOPIC_TITLE = "EXTRA_TOPIC_TITLE";

    public static final String TOPIC_CO_TICH = "co_tich";
    public static final String TOPIC_NUOC_NGOAI = "nuoc_ngoai";
    public static final String TOPIC_NGU_NGON = "ngu_ngon";
    public static final String TOPIC_GIAO_DUC = "giao_duc";
    public static final String TOPIC_PHIEU_LUU = "phieu_luu";

    private TextView tvTopicTitle;
    private TextView tvTopicDescription;
    // Removed unused btnSort field
    private RecyclerView rvStories;
    private LinearLayout layoutEmpty;
    private ImageView btnBack;

    private TopicAudiobookAdapter adapter;
    private List<Book> allStories;        // TODO: Replace with database result
    private List<Book> filteredStories;  // TODO: Replace with search/filter result
    private String currentTopicType;

    // Mini player views
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    // DATABASE INTEGRATION VARIABLES TO ADD:
    // private ApiService apiService;
    // private ProgressBar progressBar;
    // private int currentPage = 1;
    // private boolean isLoading = false;
    // private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "onCreate: Setting content view");
            setContentView(R.layout.activity_topic);

            Log.d(TAG, "onCreate: Getting intent extras");
            currentTopicType = getIntent().getStringExtra(EXTRA_TOPIC_TYPE);
            String topicTitle = getIntent().getStringExtra(EXTRA_TOPIC_TITLE);

            Log.d(TAG, "onCreate: topicType=" + currentTopicType + ", title=" + topicTitle);

            if (currentTopicType == null || topicTitle == null) {
                Log.e(TAG, "onCreate: Topic type or title is null!");
                finish();
                return;
            }

            Log.d(TAG, "onCreate: Initializing views");
            initViews();

            Log.d(TAG, "onCreate: Setting up topic data");
            setupTopicData(currentTopicType, topicTitle);

            Log.d(TAG, "onCreate: Setting up RecyclerView");
            setupRecyclerView();

            Log.d(TAG, "onCreate: Setting up listeners");
            setupListeners();

            Log.d(TAG, "onCreate: Loading stories data");
            loadStoriesDataAsync();

            Log.d(TAG, "onCreate: Setting up bottom navigation");
            setupBottomNavigation();

            Log.d(TAG, "onCreate: Setting up mini player");
            setupMiniPlayer();

            Log.d(TAG, "onCreate: Completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: Starting...");

        tvTopicTitle = findViewById(R.id.tv_topic_title);
        Log.d(TAG, "initViews: tv_topic_title = " + (tvTopicTitle != null ? "OK" : "NULL"));

        tvTopicDescription = findViewById(R.id.tv_topic_description);
        Log.d(TAG, "initViews: tv_topic_description = " + (tvTopicDescription != null ? "OK" : "NULL"));

        rvStories = findViewById(R.id.rv_stories);
        Log.d(TAG, "initViews: rv_stories = " + (rvStories != null ? "OK" : "NULL"));

        layoutEmpty = findViewById(R.id.layout_empty);
        Log.d(TAG, "initViews: layout_empty = " + (layoutEmpty != null ? "OK" : "NULL"));

        btnBack = findViewById(R.id.btn_back);
        Log.d(TAG, "initViews: btn_back = " + (btnBack != null ? "OK" : "NULL"));

        // Check for null views
        if (tvTopicTitle == null || tvTopicDescription == null ||
            rvStories == null || layoutEmpty == null || btnBack == null) {
            String errorMsg = "One or more views are null! ";
            if (tvTopicTitle == null) errorMsg += "tv_topic_title ";
            if (tvTopicDescription == null) errorMsg += "tv_topic_description ";
            if (rvStories == null) errorMsg += "rv_stories ";
            if (layoutEmpty == null) errorMsg += "layout_empty ";
            if (btnBack == null) errorMsg += "btn_back ";

            Log.e(TAG, "initViews: " + errorMsg);
            throw new RuntimeException("Failed to initialize views: " + errorMsg);
        }

        Log.d(TAG, "initViews: All views initialized successfully");
    }

    /**
     * Thiết lập thông tin topic (title, descriptiom)
     * TODO: Lấy topic metadata từ database
     */
    private void setupTopicData(String topicType, String topicTitle) {
        if (tvTopicTitle != null) {
            tvTopicTitle.setText(topicTitle);
        }

        String description;
        switch (topicType) {
            case TOPIC_CO_TICH:
                description = "Khám phá thế giới cổ tích với những câu chuyện kỳ diệu và bài học ý nghĩa";
                break;
            case TOPIC_NUOC_NGOAI:
                description = "Những câu chuyện nước ngoài nổi tiếng được yêu thích khắp thế giới";
                break;
            case TOPIC_NGU_NGON:
                description = "Học hỏi từ những câu chuyện ngụ ngôn với bài học đạo đức sâu sắc";
                break;
            case TOPIC_GIAO_DUC:
                description = "Những câu chuyện giáo dục giúp trẻ phát triển tư duy và kỹ năng sống";
                break;
            case TOPIC_PHIEU_LUU:
                description = "Những cuộc phiêu lưu thú vị kích thích trí tưởng tượng của trẻ";
                break;
            default:
                description = "Khám phá thế giới truyện với những câu chuyện hay và ý nghĩa";
        }

        if (tvTopicDescription != null) {
            tvTopicDescription.setText(description);
        }
    }

    private void setupRecyclerView() {
        allStories = new ArrayList<>();
        filteredStories = new ArrayList<>();

        adapter = new TopicAudiobookAdapter(filteredStories, item -> {
            // Mở chi tiết audiobook
            Intent intent = new Intent(TopicActivity.this, AudiobookDetailActivity.class);
            intent.putExtra("audiobook_title", item.getTitle());
            intent.putExtra("audiobook_author", item.getAuthor());
            intent.putExtra("audiobook_cover", item.getCoverUrl());
            // TODO: Truyền database ID để load chi tiết từ server
            // intent.putExtra("audiobook_id", item.getId()); // Database ID
            // intent.putExtra("audiobook_url", item.getAudioUrl()); // Firebase URL

            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvStories.setLayoutManager(layoutManager);
        rvStories.setAdapter(adapter);
    }

    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // TODO: Sắp xếp (tạm thời disable)
        // btnSort.setOnClickListener(v -> showSortOptions());
    }

    private void loadStoriesDataAsync() {
        // Hiển thị empty state ban đầu
        rvStories.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        // Load dữ liệu trong background thread
        new Thread(() -> {
            try {
                // Load dữ liệu dựa trên topic type
                allStories.clear();

                switch (currentTopicType) {
                    case TOPIC_CO_TICH:
                        loadCoTichStories();
                        break;
                    case TOPIC_NUOC_NGOAI:
                        loadNuocNgoaiStories();
                        break;
                    case TOPIC_NGU_NGON:
                        loadNguNgonStories();
                        break;
                    case TOPIC_GIAO_DUC:
                        loadGiaoDucStories();
                        break;
                    case TOPIC_PHIEU_LUU:
                        loadPhieuLuuStories();
                        break;
                }

                // Update UI on main thread
                runOnUiThread(() -> {
                    try {
                        filteredStories.clear();
                        filteredStories.addAll(allStories);

                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

                        // Hiển thị dữ liệu hoặc empty state
                        if (filteredStories.isEmpty()) {
                            rvStories.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvStories.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI", e);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading stories data", e);
                runOnUiThread(() -> {
                    rvStories.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void loadCoTichStories() {
        allStories.add(new Book("1", "Cô Bé Quàng Khăn Đỏ", "Anh em Grimm", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_co_tich, "Câu chuyện cổ tích nổi tiếng về cô bé quàng khăn đỏ", TOPIC_CO_TICH));
        allStories.add(new Book("2", "Tấm Cám", "Dân gian Việt Nam", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_co_tich, "Câu chuyện cổ tích Việt Nam về Tấm và Cám", TOPIC_CO_TICH));
        allStories.add(new Book("3", "Cô Bé Lọ Lem", "Charles Perrault", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_co_tich, "Câu chuyện cổ tích về cô bé Lọ Lem", TOPIC_CO_TICH));
        allStories.add(new Book("4", "Thạch Sanh", "Dân gian Việt Nam", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_co_tich, "Câu chuyện cổ tích Việt Nam về Thạch Sanh", TOPIC_CO_TICH));
    }

    private void loadNuocNgoaiStories() {
        allStories.add(new Book("5", "Ba Chú Heo Con", "Joseph Jacobs", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_nuoc_ngoai, "Câu chuyện nước ngoài về ba chú heo con", TOPIC_NUOC_NGOAI));
        allStories.add(new Book("6", "Nàng Bạch Tuyết", "Anh em Grimm", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_nuoc_ngoai, "Câu chuyện cổ tích nước ngoài về Bạch Tuyết", TOPIC_NUOC_NGOAI));
        allStories.add(new Book("7", "Người Đẹp và Quái Vật", "Gabrielle-Suzanne", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_nuoc_ngoai, "Câu chuyện cổ tích về Người đẹp và Quái vật", TOPIC_NUOC_NGOAI));
    }

    private void loadNguNgonStories() {
        allStories.add(new Book("8", "Rùa và Thỏ", "Aesop", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_ngu_ngon, "Câu chuyện ngụ ngôn về cuộc đua rùa và thỏ", TOPIC_NGU_NGON));
        allStories.add(new Book("9", "Kiến và Châu Chấu", "Aesop", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_ngu_ngon, "Câu chuyện ngụ ngôn về sự chăm chỉ", TOPIC_NGU_NGON));
        allStories.add(new Book("10", "Sư Tử và Chuột", "Aesop", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_ngu_ngon, "Câu chuyện ngụ ngôn về lòng biết ơn", TOPIC_NGU_NGON));
    }

    private void loadGiaoDucStories() {
        allStories.add(new Book("11", "Học Cách Chia Sẻ", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_giao_duc, "Câu chuyện giáo dục về tình chia sẻ", TOPIC_GIAO_DUC));
        allStories.add(new Book("12", "Tầm Quan Trọng Của Sự Thật", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_giao_duc, "Câu chuyện giáo dục về sự trung thực", TOPIC_GIAO_DUC));
        allStories.add(new Book("13", "Làm Bạn Với Mọi Người", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_giao_duc, "Câu chuyện giáo dục về tình bạn", TOPIC_GIAO_DUC));
    }

    private void loadPhieuLuuStories() {
        allStories.add(new Book("14", "Cuộc Phiêu Lưu Trong Rừng", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_phieu_luu, "Câu chuyện phiêu lưu thú vị trong rừng", TOPIC_PHIEU_LUU));
        allStories.add(new Book("15", "Khám Phá Đại Dương", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_phieu_luu, "Câu chuyện phiêu lưu dưới đáy đại dương", TOPIC_PHIEU_LUU));
        allStories.add(new Book("16", "Hành Trình Tới Vũ Trụ", "Tác giả ẩn danh", "android.resource://com.example.audiobook_for_kids/" + R.drawable.ic_phieu_luu, "Câu chuyện phiêu lưu trong không gian vũ trụ", TOPIC_PHIEU_LUU));
    }



    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Set mặc định chọn tab Home
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                Intent intent = new Intent(TopicActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                Intent intent = new Intent(TopicActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(TopicActivity.this, AIStoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_library) {
                Intent intent = new Intent(TopicActivity.this, LibraryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(TopicActivity.this, AccountActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
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
