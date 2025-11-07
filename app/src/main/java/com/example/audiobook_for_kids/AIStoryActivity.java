package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AIStoryActivity extends AppCompatActivity {

    private EditText etPrompt;
    private Button btnGenerate;
    private ProgressBar progressBar;
    private CardView cardResult;
    private TextView tvStoryTitle;
    private TextView tvStoryContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_story);

        // Ánh xạ views
        etPrompt = findViewById(R.id.et_prompt);
        btnGenerate = findViewById(R.id.btn_generate);
        progressBar = findViewById(R.id.progress_bar);
        cardResult = findViewById(R.id.card_result);
        tvStoryTitle = findViewById(R.id.tv_story_title);
        tvStoryContent = findViewById(R.id.tv_story_content);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnRegenerate = findViewById(R.id.btn_regenerate);

        // Xử lý thay đổi text trong EditText
        etPrompt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable/disable button dựa vào text
                btnGenerate.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Xử lý nút tạo truyện
        btnGenerate.setOnClickListener(v -> generateStory());

        // Xử lý nút tạo lại
        btnRegenerate.setOnClickListener(v -> generateStory());

        // Xử lý nút lưu
        btnSave.setOnClickListener(v -> saveStory());

        // Disable button ban đầu
        btnGenerate.setEnabled(false);

        // Xử lý Bottom Navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ai);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_ai) {
                // Đã ở trang AI
                return true;
            } else if (itemId == R.id.nav_search) {
                Intent intent = new Intent(AIStoryActivity.this, SearchActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // TODO: Thêm xử lý cho Thư viện
                return true;
            }

            return false;
        });
    }

    private void generateStory() {
        String prompt = etPrompt.getText().toString().trim();

        if (prompt.isEmpty()) {
            return;
        }

        // Hiển thị progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        cardResult.setVisibility(View.GONE);

        // TODO: Gọi API AI để tạo truyện
        // Giả lập việc tạo truyện (thay bằng API call thực tế)
        simulateStoryGeneration(prompt);
    }

    private void simulateStoryGeneration(String prompt) {
        // Giả lập delay của API call
        new android.os.Handler().postDelayed(() -> {
            // Ẩn progress bar
            progressBar.setVisibility(View.GONE);
            btnGenerate.setEnabled(true);

            // Hiển thị kết quả (giả lập)
            showStoryResult(
                "Câu chuyện về " + prompt,
                "Ngày xửa ngày xưa, có một câu chuyện về " + prompt + "...\n\n" +
                "Đây là nội dung truyện được AI tạo ra dựa trên yêu cầu của bạn. " +
                "Trong tương lai, đây sẽ là nội dung thực sự từ AI.\n\n" +
                "Truyện sẽ có nhiều đoạn văn thú vị và phù hợp với lứa tuổi thiếu nhi."
            );
        }, 2000); // 2 giây delay
    }

    private void showStoryResult(String title, String content) {
        tvStoryTitle.setText(title);
        tvStoryContent.setText(content);
        cardResult.setVisibility(View.VISIBLE);
    }

    private void saveStory() {
        // TODO: Lưu truyện vào database hoặc shared preferences
        // Hiển thị thông báo đã lưu
        android.widget.Toast.makeText(this, "Đã lưu truyện!", android.widget.Toast.LENGTH_SHORT).show();
    }
}
