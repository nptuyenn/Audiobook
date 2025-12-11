package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvFeatured;
    private RecyclerView rvSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvFeatured = findViewById(R.id.rv_featured);
        rvSuggestions = findViewById(R.id.rv_suggestions);

        setupFeaturedRecycler();

        setupSuggestionsRecycler();

        setupBottomNavigation();

        try {
            setupTopicClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void setupFeaturedRecycler() {
        // Cấu hình LayoutManager (cuộn ngang)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFeatured.setLayoutManager(layoutManager);

        // Tạo dữ liệu giả - uncomment khi có drawable
        // List<Integer> featuredData = new ArrayList<>();
        // featuredData.add(R.drawable.cover_large_1);
        // featuredData.add(R.drawable.cover_large_2);
        // featuredData.add(R.drawable.cover_large_3);
        // AudiobookAdapter featuredAdapter = new AudiobookAdapter(this, featuredData);
        // rvFeatured.setAdapter(featuredAdapter);
    }

    private void setupSuggestionsRecycler() {
        // Cấu hình LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvSuggestions.setLayoutManager(layoutManager);

        // Tạo dữ liệu giả - uncomment khi có drawable
        // List<Integer> suggestionsData = new ArrayList<>();
        // suggestionsData.add(R.drawable.cover_small_1);
        // suggestionsData.add(R.drawable.cover_small_2);
        // suggestionsData.add(R.drawable.cover_small_3);
        // AudiobookAdapter suggestionsAdapter = new AudiobookAdapter(this, suggestionsData);
        // rvSuggestions.setAdapter(suggestionsAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == R.id.nav_ai) {
                Intent intent = new Intent(MainActivity.this, AIStoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_library) {
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
            e.printStackTrace();
        }
    }
}