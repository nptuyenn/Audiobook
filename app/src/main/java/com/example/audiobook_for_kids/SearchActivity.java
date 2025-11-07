package com.example.audiobook_for_kids;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView ivBack;
    private ImageView ivClearSearch;
    private RecyclerView rvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Ánh xạ views
        etSearch = findViewById(R.id.et_search);
        ivBack = findViewById(R.id.iv_back);
        ivClearSearch = findViewById(R.id.iv_clear_search);
        rvSearchResults = findViewById(R.id.rv_search_results);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Xử lý nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Xử lý nút xóa text tìm kiếm
        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));

        // Xử lý tìm kiếm khi nhập text
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hiển thị/ẩn nút xóa
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // TODO: Thực hiện tìm kiếm
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Focus vào ô tìm kiếm khi mở activity
        etSearch.requestFocus();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvSearchResults.setLayoutManager(layoutManager);

        // TODO: Thiết lập adapter khi có dữ liệu
        // SearchAdapter adapter = new SearchAdapter(this, searchResults);
        // rvSearchResults.setAdapter(adapter);
    }

    private void performSearch(String query) {
        // TODO: Thực hiện tìm kiếm với query
        // Cập nhật RecyclerView với kết quả tìm kiếm
    }
}

