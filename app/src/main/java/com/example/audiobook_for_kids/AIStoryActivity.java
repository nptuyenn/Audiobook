package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.auth.SessionManager;
import com.example.audiobook_for_kids.model.requests.AIStoryRequest;
import com.example.audiobook_for_kids.model.responses.AIStoryResponse;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIStoryActivity extends AppCompatActivity {

    private static final String TAG = "AIStoryActivity";
    private EditText etPrompt;
    private Button btnGenerate, btnListen, btnSave, btnRegenerate;
    private ProgressBar progressBar;
    private CardView cardResult;
    private TextView tvStoryTitle, tvStoryContent;
    private ImageButton btnOpenChat;

    private String currentAudioBase64;
    private File tempAudioFile;

    // Mini player views
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

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
        btnListen = findViewById(R.id.btn_listen);
        btnSave = findViewById(R.id.btn_save);
        btnRegenerate = findViewById(R.id.btn_regenerate);
        btnOpenChat = findViewById(R.id.btn_open_chat);

        etPrompt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnGenerate.setEnabled(s.length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGenerate.setOnClickListener(v -> generateStory());
        btnRegenerate.setOnClickListener(v -> generateStory());
        btnListen.setOnClickListener(v -> playGeneratedAudio());
        btnSave.setOnClickListener(v -> saveStory());
        btnOpenChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, AIChatActivity.class);
            startActivity(intent);
        });

        btnGenerate.setEnabled(false);
        setupBottomNavigation();
        setupMiniPlayer();
    }

    private void generateStory() {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        cardResult.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        
        AIStoryRequest request = new AIStoryRequest(prompt, "Truyện AI");
        
        apiService.generateAIStory(token, request).enqueue(new Callback<AIStoryResponse>() {
            @Override
            public void onResponse(Call<AIStoryResponse> call, Response<AIStoryResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnGenerate.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    AIStoryResponse data = response.body();
                    currentAudioBase64 = data.getAudioBase64();
                    showStoryResult("Truyện: " + prompt, data.getText());
                    saveAudioToTempFile(currentAudioBase64);
                } else {
                    Toast.makeText(AIStoryActivity.this, "Lỗi khi tạo truyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIStoryResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnGenerate.setEnabled(true);
                Toast.makeText(AIStoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAudioToTempFile(String base64) {
        if (base64 == null) return;
        try {
            byte[] audioData = Base64.decode(base64, Base64.DEFAULT);
            tempAudioFile = File.createTempFile("ai_story", ".mp3", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                fos.write(audioData);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving temp audio", e);
        }
    }

    private void playGeneratedAudio() {
        if (tempAudioFile != null && tempAudioFile.exists()) {
            audioManager.playLocalFile(tempAudioFile.getAbsolutePath(), tvStoryTitle.getText().toString(), "AI Gấu Nhỏ");
        } else {
            Toast.makeText(this, "Chưa có file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStoryResult(String title, String content) {
        tvStoryTitle.setText(title);
        tvStoryContent.setText(content);
        cardResult.setVisibility(View.VISIBLE);
    }

    private void saveStory() {
        Toast.makeText(this, "Đã gửi yêu cầu lưu truyện!", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ai);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) { finish(); return true; }
            else if (itemId == R.id.nav_ai) return true;
            else if (itemId == R.id.nav_library) {
                startActivity(new Intent(this, LibraryActivity.class));
                finish(); return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(this, AccountActivity.class));
                finish(); return true;
            }
            return false;
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
        audioManager.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) btnMiniPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        });

        btnMiniPlay.setOnClickListener(v -> {
            Boolean playing = audioManager.getIsPlaying().getValue();
            if (playing != null && playing) audioManager.pause(); else audioManager.play();
        });
    }
}
