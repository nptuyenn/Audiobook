package com.example.audiobook_for_kids;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.example.audiobook_for_kids.model.requests.AISaveStoryRequest;
import com.example.audiobook_for_kids.model.requests.AIStoryRequest;
import com.example.audiobook_for_kids.model.requests.AIVoiceRequest;
import com.example.audiobook_for_kids.model.responses.AIChatResponse;
import com.example.audiobook_for_kids.model.responses.AIStoryResponse;
import com.example.audiobook_for_kids.model.responses.TranscribeResponse;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.example.audiobook_for_kids.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIStoryActivity extends AppCompatActivity {

    private static final String TAG = "AIStoryActivity";
    private static final int REQUEST_RECORD_AUDIO = 100;
    
    // Audio Configuration for Azure STT
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    private EditText etPrompt, etChatMessage;
    private Button btnGenerate, btnStopVoice;
    private ImageButton btnMicStory, btnMicChat, btnSend, btnListen, btnSave, btnRegenerate;
    private ProgressBar progressBar, progressChat;
    private CardView cardResult;
    private ConstraintLayout containerStoryMode, containerChatMode;
    private FrameLayout layoutVoiceOverlay;
    private TextView tvStoryTitle, tvStoryContent, aiHeader, tvVoiceStatus;
    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    
    private String currentAudioUrl, currentStoryText, currentStoryTitle;
    private AudioRecord audioRecord;
    private MediaRecorder mediaRecorder; 
    private Thread recordingThread;
    private String recordFilePath;
    private AudioPlaybackManager audioManager;
    private boolean isRecordingForStory = true;
    private boolean isRecording = false;

    // Navigation Drawer views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Mini player views
    private CardView layoutMiniPlayer;
    private ImageView ivMiniCover;
    private TextView tvMiniTitle, tvMiniAuthor;
    private ImageButton btnMiniPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_story);

        initViews();
        setupChat();
        setupListeners();
        setupDrawerListeners();
        setupMiniPlayer();
        setupBottomNavigation();
        
        recordFilePath = getExternalCacheDir().getAbsolutePath() + "/voice_input.m4a";
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        etPrompt = findViewById(R.id.et_prompt);
        btnGenerate = findViewById(R.id.btn_generate);
        btnMicStory = findViewById(R.id.btn_mic_story);
        progressBar = findViewById(R.id.progress_bar);
        cardResult = findViewById(R.id.card_result);
        tvStoryTitle = findViewById(R.id.tv_story_title);
        tvStoryContent = findViewById(R.id.tv_story_content);
        btnListen = findViewById(R.id.btn_listen);
        btnSave = findViewById(R.id.btn_save);
        btnRegenerate = findViewById(R.id.btn_regenerate);
        
        containerStoryMode = findViewById(R.id.container_story_mode);
        containerChatMode = findViewById(R.id.container_chat_mode);
        aiHeader = findViewById(R.id.ai_header);
        
        rvChat = findViewById(R.id.rv_chat);
        etChatMessage = findViewById(R.id.et_message);
        btnMicChat = findViewById(R.id.btn_mic_chat);
        btnSend = findViewById(R.id.btn_send);
        progressChat = findViewById(R.id.progress_chat);

        layoutVoiceOverlay = findViewById(R.id.layout_voice_overlay);
        tvVoiceStatus = findViewById(R.id.tv_voice_status);
        btnStopVoice = findViewById(R.id.btn_stop_voice);

        switchToStoryMode();
    }

    private void setupChat() {
        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
        if (chatMessages.isEmpty()) {
            addChatMessage("Chào bé! Gấu Nhỏ đây, bé muốn tâm sự gì không?", false);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        btnGenerate.setOnClickListener(v -> generateStory());
        btnRegenerate.setOnClickListener(v -> generateStory());
        btnListen.setOnClickListener(v -> playGeneratedAudio());
        btnSave.setOnClickListener(v -> saveStoryToLibrary());
        
        btnMicStory.setOnClickListener(v -> startVoiceInput(true));
        btnMicChat.setOnClickListener(v -> startVoiceInput(false));
        btnStopVoice.setOnClickListener(v -> stopVoiceInput());
        btnSend.setOnClickListener(v -> sendChatMessage());
    }

    private void setupDrawerListeners() {
        View headerView = navigationView.getHeaderView(0);
        
        View btnNavStory = headerView.findViewById(R.id.btn_nav_story);
        View btnNavChat = headerView.findViewById(R.id.btn_nav_chat);

        btnNavStory.setOnClickListener(v -> {
            switchToStoryMode();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        btnNavChat.setOnClickListener(v -> {
            switchToChatMode();
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void switchToStoryMode() {
        containerStoryMode.setVisibility(View.VISIBLE);
        containerChatMode.setVisibility(View.GONE);
        aiHeader.setText("AI Tạo Truyện");
    }

    private void switchToChatMode() {
        containerStoryMode.setVisibility(View.GONE);
        containerChatMode.setVisibility(View.VISIBLE);
        aiHeader.setText("Chat với Gấu Nhỏ");
    }

    private void startVoiceInput(boolean isStory) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }

        isRecordingForStory = isStory;
        layoutVoiceOverlay.setVisibility(View.VISIBLE);
        tvVoiceStatus.setText("Gấu Nhỏ đang lắng nghe...");
        
        startRecordingCompressed();
    }

    private void startRecordingCompressed() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(16000);
            mediaRecorder.setAudioEncodingBitRate(32000); 
            mediaRecorder.setOutputFile(recordFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            Log.e(TAG, "Lỗi ghi âm", e);
            layoutVoiceOverlay.setVisibility(View.GONE);
        }
    }

    private void stopVoiceInput() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                sendVoiceToServer();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi dừng ghi âm", e);
            }
        }
        layoutVoiceOverlay.setVisibility(View.GONE);
    }

    private void sendVoiceToServer() {
        String base64Audio = encodeFileToBase64(recordFilePath);
        if (base64Audio == null) return;

        if (isRecordingForStory) progressBar.setVisibility(View.VISIBLE);
        else progressChat.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();

        api.generateAIStoryFromVoice(token, new AIVoiceRequest(base64Audio)).enqueue(new Callback<TranscribeResponse>() {
            @Override
            public void onResponse(Call<TranscribeResponse> call, Response<TranscribeResponse> response) {
                progressBar.setVisibility(View.GONE);
                progressChat.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String resultText = response.body().getTranscribedText();
                    if (resultText != null && !resultText.isEmpty()) {
                        if (isRecordingForStory) etPrompt.setText(resultText);
                        else etChatMessage.setText(resultText);
                        Toast.makeText(AIStoryActivity.this, "Gấu Nhỏ nghe được: " + resultText, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AIStoryActivity.this, "Gấu Nhỏ không nghe rõ, bé nói lại nhé", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<TranscribeResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                progressChat.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi STT", t);
            }
        });
    }

    private String encodeFileToBase64(String path) {
        try {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes);
            fis.close();
            // Đã có import android.util.Base64 ở phía trên
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            return null;
        }
    }

    private void generateStory() {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "Hãy nói hoặc nhập gì đó để Gấu Nhỏ kể chuyện nhé!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();

        api.generateAIStory(token, new AIStoryRequest(prompt, "Truyện AI")).enqueue(new Callback<AIStoryResponse>() {
            @Override
            public void onResponse(Call<AIStoryResponse> call, Response<AIStoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentStoryText = response.body().getText();
                    currentStoryTitle = "Truyện: " + prompt;

                    tvStoryTitle.setText(currentStoryTitle);
                    tvStoryContent.setText(currentStoryText);
                    cardResult.setVisibility(View.VISIBLE);

                    currentAudioUrl = response.body().getAudioUrl();
                    if (currentAudioUrl == null || currentAudioUrl.isEmpty()) {
                        generateStoryAudio(currentStoryText);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
            @Override public void onFailure(Call<AIStoryResponse> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void generateStoryAudio(String storyText) {
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.generateStoryAudio(token, new HashMap<String, String>() {{
            put("storyContent", storyText);
        }}).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentAudioUrl = response.body().get("audioUrl");
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveStoryToLibrary() {
        if (currentStoryText == null || currentAudioUrl == null) {
            Toast.makeText(this, "Vui lòng đợi âm thanh chuẩn bị xong", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        AISaveStoryRequest req = new AISaveStoryRequest(currentStoryTitle, currentStoryText, currentAudioUrl);
        api.saveAIStory(token, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AIStoryActivity.this, "Đã lưu vào thư viện!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AIStoryActivity.this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AIStoryActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playGeneratedAudio() {
        if (currentAudioUrl == null || currentAudioUrl.isEmpty()) {
            Toast.makeText(this, "Chưa có âm thanh, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullUrl = getFullUrl(currentAudioUrl);
        Log.d(TAG, "Đang phát audio từ: " + fullUrl);

        if (audioManager != null) {
            audioManager.setAudioSource(fullUrl, currentStoryTitle, "AI Gen", "", "");
        }
    }

    private String getFullUrl(String url) {
        if (url == null || url.isEmpty() || url.startsWith("http")) return url;
        String baseUrl = Constants.BASE_URL;
        if (!baseUrl.endsWith("/") && !url.startsWith("/")) baseUrl += "/";
        else if (baseUrl.endsWith("/") && url.startsWith("/")) url = url.substring(1);
        return baseUrl + url;
    }

    private void sendChatMessage() {
        String msg = etChatMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        addChatMessage(msg, true);
        etChatMessage.setText("");
        progressChat.setVisibility(View.VISIBLE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        
        api.chatWithAI(token, new AIChatRequest(msg, new ArrayList<>())).enqueue(new Callback<AIChatResponse>() {
            @Override
            public void onResponse(Call<AIChatResponse> call, Response<AIChatResponse> response) {
                progressChat.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    addChatMessage(response.body().getText(), false);
                }
            }
            @Override public void onFailure(Call<AIChatResponse> call, Throwable t) { progressChat.setVisibility(View.GONE); }
        });
    }

    private void addChatMessage(String text, boolean isUser) {
        chatMessages.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChat.scrollToPosition(chatMessages.size() - 1);
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
        audioManager.getCurrentCover().observe(this, coverUrl -> {
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(this).load(coverUrl).placeholder(R.drawable.ic_headphone_placeholder).into(ivMiniCover);
            }
        });
        audioManager.getIsPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) btnMiniPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        });

        btnMiniPlay.setOnClickListener(v -> {
            Boolean playing = audioManager.getIsPlaying().getValue();
            if (playing != null && playing) audioManager.pause(); else audioManager.play();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ai);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { finish(); return true; }
            if (id == R.id.nav_library) { startActivity(new Intent(this, LibraryActivity.class)); finish(); return true; }
            if (id == R.id.nav_account) { startActivity(new Intent(this, AccountActivity.class)); finish(); return true; }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
    }
}
