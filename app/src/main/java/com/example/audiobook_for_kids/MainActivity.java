// File: MainActivity.java (ĐÃ SỬA HOÀN CHỈNH)
package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiobook_for_kids.adapter.AudiobookAdapter;
import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvFeatured;
    private RecyclerView rvSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvFeatured = findViewById(R.id.rv_featured);
        rvSuggestions = findViewById(R.id.rv_suggestions);

        // Cấu hình LayoutManager (cuộn ngang)
        rvFeatured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // GỌI API THẬT — CHỈ GỌI 1 LẦN
        loadBooks();

        // Bottom Navigation
        setupBottomNavigation();

        // Nút Login
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });
    }

    private void loadBooks() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<List<Book>> call = api.getBooks();

        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = response.body();

                    // Featured (5 cuốn đầu)
                    AudiobookAdapter featuredAdapter = new AudiobookAdapter(
                            MainActivity.this,
                            books.subList(0, Math.min(5, books.size())),
                            book -> openBookDetail(book)
                    );
                    rvFeatured.setAdapter(featuredAdapter);

                    // Suggestions (từ cuốn 6 trở đi)
                    if (books.size() > 5) {
                        AudiobookAdapter suggestionsAdapter = new AudiobookAdapter(
                                MainActivity.this,
                                books.subList(5, Math.min(10, books.size())),
                                book -> openBookDetail(book)
                        );
                        rvSuggestions.setAdapter(suggestionsAdapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openBookDetail(Book book) {
        Intent intent = new Intent(MainActivity.this, AudiobookDetailActivity.class);
        intent.putExtra("BOOK_ID", book.getId());
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_COVER", book.getCoverUrl());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        startActivity(intent);
    }
    private void setupBottomNavigation() {
        // FIX LỖI 1: Thay 'bottomNavigation' bằng biến đã khai báo 'bottomNav'
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_search) startActivity(new Intent(this, SearchActivity.class));
            else if (id == R.id.nav_ai) startActivity(new Intent(this, AIStoryActivity.class));
            else if (id == R.id.nav_library) startActivity(new Intent(this, LibraryActivity.class));
            else if (id == R.id.nav_account) startActivity(new Intent(this, AccountActivity.class));
            else if (id == R.id.nav_home) return true;
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}