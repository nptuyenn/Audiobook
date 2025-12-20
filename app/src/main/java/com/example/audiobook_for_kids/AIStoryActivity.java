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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiobook_for_kids.adapter.ChatAdapter;
import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.auth.SessionManager;
import com.example.audiobook_for_kids.model.ChatMessage;
import com.example.audiobook_for_kids.model.requests.AIChatRequest;
import com.example.audiobook_for_kids.model.requests.AIStoryRequest;
import com.example.audiobook_for_kids.model.responses.AIChatResponse;
import com.example.audiobook_for_kids.model.responses.AIStoryResponse;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIStoryActivity extends AppCompatActivity {

    private static final String TAG = "AIStoryActivity";

    // === MENU & NAVIGATION VIEWS ===
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;
    private TextView tvHeaderTitle; // ID: ai_header
    private ConstraintLayout containerStoryMode;
    private ConstraintLayout containerChatMode;

    // === STORY MODE VIEWS ===
    private EditText etPrompt;
    private Button btnGenerate;
    private ProgressBar progressBarStory; // ID: progress_bar
    private CardView cardResult;
    private TextView tvStoryTitle, tvStoryContent;

    // 3 nút chức năng tròn
    private ImageButton btnListen, btnSave, btnRegenerate;

    private String currentAudioBase64;
    private File tempAudioFile;

    // === CHAT MODE VIEWS ===
    private RecyclerView rvChat;
    private EditText etMessageChat; // ID: et_message
    private ImageButton btnSendChat; // ID: btn_send
    private ProgressBar progressBarChat; // ID: progress_chat

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private List<String> chatHistory;

    // === MINI PLAYER VIEWS ===
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;
    private AudioPlaybackManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_story);

        initViews();
        setupMenu();
        setupStoryLogic();
        setupChatLogic();
        setupBottomNavigation();
        setupMiniPlayer();
    }

    private void initViews() {
        // 1. Menu & Layout Containers
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenu = findViewById(R.id.btn_menu);
        tvHeaderTitle = findViewById(R.id.ai_header);

        containerStoryMode = findViewById(R.id.container_story_mode);
        containerChatMode = findViewById(R.id.container_chat_mode);

        // 2. Story Views
        etPrompt = findViewById(R.id.et_prompt);
        btnGenerate = findViewById(R.id.btn_generate);
        progressBarStory = findViewById(R.id.progress_bar);
        cardResult = findViewById(R.id.card_result);
        tvStoryTitle = findViewById(R.id.tv_story_title);
        tvStoryContent = findViewById(R.id.tv_story_content);

        // 3 nút tròn
        btnListen = findViewById(R.id.btn_listen);
        btnSave = findViewById(R.id.btn_save);
        btnRegenerate = findViewById(R.id.btn_regenerate);

        // 3. Chat Views
        rvChat = findViewById(R.id.rv_chat);
        etMessageChat = findViewById(R.id.et_message);
        btnSendChat = findViewById(R.id.btn_send);
        progressBarChat = findViewById(R.id.progress_chat);
    }

    private void setupMenu() {
        // Sự kiện mở menu
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Sự kiện chọn item trong menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            // Lưu ý: Đảm bảo file menu/drawer_menu.xml của bạn có các ID này
            if (id == R.id.nav_create_story) { // Giả sử ID menu tạo truyện là nav_create_story
                switchToStoryMode();
            } else if (id == R.id.nav_chat) { // Giả sử ID menu chat là nav_chat
                switchToChatMode();
            }
            item.setChecked(true);

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void switchToStoryMode() {
        containerStoryMode.setVisibility(View.VISIBLE);
        containerChatMode.setVisibility(View.GONE);
        tvHeaderTitle.setText("AI Tạo Truyện");
    }

    private void switchToChatMode() {
        containerStoryMode.setVisibility(View.GONE);
        containerChatMode.setVisibility(View.VISIBLE);
        tvHeaderTitle.setText("Chat với Gấu Nhỏ");
    }

    // ================= LOGIC STORY (Giữ nguyên logic cũ) =================
    private void setupStoryLogic() {
        etPrompt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnGenerate.setEnabled(s.length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnGenerate.setEnabled(false);
        btnGenerate.setOnClickListener(v -> generateStory());
        btnRegenerate.setOnClickListener(v -> generateStory());
        btnListen.setOnClickListener(v -> playGeneratedAudio());
        btnSave.setOnClickListener(v -> saveStory());
    }

    private void generateStory() {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) return;

        progressBarStory.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        cardResult.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();

        AIStoryRequest request = new AIStoryRequest(prompt, "Truyện AI");

        apiService.generateAIStory(token, request).enqueue(new Callback<AIStoryResponse>() {
            @Override
            public void onResponse(Call<AIStoryResponse> call, Response<AIStoryResponse> response) {
                progressBarStory.setVisibility(View.GONE);
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
                progressBarStory.setVisibility(View.GONE);
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

    // ================= LOGIC CHAT (Lấy từ AIChatActivity) =================
    private void setupChatLogic() {
        messageList = new ArrayList<>();
        chatHistory = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        // Tin nhắn chào mừng
        addMessage("Chào bé! Gấu Nhỏ đây, bé muốn hỏi gì gấu không?", false);

        btnSendChat.setOnClickListener(v -> sendChatMessage());
    }

    private void sendChatMessage() {
        String msg = etMessageChat.getText().toString().trim();
        if (msg.isEmpty()) return;

        addMessage(msg, true);
        etMessageChat.setText("");
        progressBarChat.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();

        AIChatRequest request = new AIChatRequest(msg, chatHistory);

        apiService.chatWithAI(token, request).enqueue(new Callback<AIChatResponse>() {
            @Override
            public void onResponse(Call<AIChatResponse> call, Response<AIChatResponse> response) {
                progressBarChat.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getText();
                    addMessage(answer, false);
                    chatHistory.add("User: " + msg);
                    chatHistory.add("AI: " + answer);
                } else {
                    Toast.makeText(AIStoryActivity.this, "Gấu Nhỏ đang bận rồi...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIChatResponse> call, Throwable t) {
                progressBarChat.setVisibility(View.GONE);
                Toast.makeText(AIStoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    // ================= CÁC THÀNH PHẦN CHUNG (BottomNav, MiniPlayer) =================
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
        // Lưu ý: Bên trong layout_mini_player cần có các ID này
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