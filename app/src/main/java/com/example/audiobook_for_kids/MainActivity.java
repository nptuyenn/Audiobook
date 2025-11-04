package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

        // Ánh xạ
        rvFeatured = findViewById(R.id.rv_featured);
        rvSuggestions = findViewById(R.id.rv_suggestions);

        // Thiết lập RecyclerView lớn (Featured)
        setupFeaturedRecycler();

        // Thiết lập RecyclerView nhỏ (Suggestions)
        setupSuggestionsRecycler();

        // Xử lý Bottom Navigation
        setupBottomNavigation();
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
        bottomNav.setOnItemSelectedListener(item -> {
            // Xử lý khi chọn các tab khác nhau
            // TODO: Thêm navigation logic ở đây
            return true;
        });
    }
}