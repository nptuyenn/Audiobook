package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
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

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo tab Home được chọn khi quay lại MainActivity
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
        // Set mặc định chọn tab Home
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                // Mở SearchActivity khi nhấn vào Tìm kiếm
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                // Đã ở trang chủ
                return true;
            } else if (itemId == R.id.nav_ai) {
                // Mở AIStoryActivity khi nhấn vào AI
                Intent intent = new Intent(MainActivity.this, AIStoryActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_library) {
                // TODO: Thêm xử lý cho Thư viện
                Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }
}