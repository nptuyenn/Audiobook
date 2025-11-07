package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class LibraryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Ánh xạ views
        tabLayout = findViewById(R.id.tab_layout);
        rvLibrary = findViewById(R.id.rv_library);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập TabLayout
        setupTabs();

        // Xử lý Bottom Navigation
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đảm bảo tab Library được chọn
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_library);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvLibrary.setLayoutManager(layoutManager);

        // TODO: Thiết lập adapter khi có dữ liệu
        // LibraryAdapter adapter = new LibraryAdapter(this, libraryItems);
        // rvLibrary.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đã lưu"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã nghe"));
        tabLayout.addTab(tabLayout.newTab().setText("Yêu thích"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO: Lọc dữ liệu theo tab được chọn
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
        // TODO: Load dữ liệu theo tab
        // tabPosition: 0 = Đã lưu, 1 = Đã nghe, 2 = Yêu thích
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
            }

            return false;
        });
    }
}

