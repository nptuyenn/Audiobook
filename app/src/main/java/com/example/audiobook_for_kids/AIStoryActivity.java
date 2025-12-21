package com.example.audiobook_for_kids;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import com.example.audiobook_for_kids.model.responses.AIChatResponse;
import com.example.audiobook_for_kids.model.responses.AIStoryResponse;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
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
    private static final int REQUEST_RECORD_AUDIO = 100;
    
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
    
    private String currentAudioBase64, currentStoryText, currentStoryTitle;
    private File tempAudioFile;
    private SpeechRecognizer speechRecognizer;
    private AudioPlaybackManager audioManager;
    private boolean isRecordingForStory = true;

    // Mini player views (for setupMiniPlayer)
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
        setupMiniPlayer();
        setupBottomNavigation();
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    }

    private void initViews() {
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
            if (containerStoryMode.getVisibility() == View.VISIBLE) switchToChatMode();
            else switchToStoryMode();
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
        tvVoiceStatus.setText("Hãy nói gì đó nhé!");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { tvVoiceStatus.setText("Đang nghe..."); }
            @Override public void onResults(Bundle results) {
                layoutVoiceOverlay.setVisibility(View.GONE);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    if (isRecordingForStory) etPrompt.setText(text);
                    else etChatMessage.setText(text);
                }
            }
            @Override public void onError(int error) {
                layoutVoiceOverlay.setVisibility(View.GONE);
                Log.e(TAG, "Speech Error: " + error);
                Toast.makeText(AIStoryActivity.this, "Gấu Nhỏ không nghe rõ, bé nói lại nhé!", Toast.LENGTH_SHORT).show();
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() { layoutVoiceOverlay.setVisibility(View.GONE); }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
        speechRecognizer.startListening(intent);
    }

    private void stopVoiceInput() {
        speechRecognizer.stopListening();
        layoutVoiceOverlay.setVisibility(View.GONE);
    }

    private void generateStory() {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        
        api.generateAIStory(token, new AIStoryRequest(prompt, "Truyện AI")).enqueue(new Callback<AIStoryResponse>() {
            @Override
            public void onResponse(Call<AIStoryResponse> call, Response<AIStoryResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentStoryText = response.body().getText();
                    currentAudioBase64 = response.body().getAudioBase64();
                    currentStoryTitle = "Truyện: " + prompt;
                    
                    tvStoryTitle.setText(currentStoryTitle);
                    tvStoryContent.setText(currentStoryText);
                    cardResult.setVisibility(View.VISIBLE);
                    saveAudioToTempFile(currentAudioBase64);
                }
            }
            @Override public void onFailure(Call<AIStoryResponse> call, Throwable t) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void saveStoryToLibrary() {
        if (currentStoryText == null || currentAudioBase64 == null) return;
        
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        ApiService api = ApiClient.getClient().create(ApiService.class);
        
        AISaveStoryRequest req = new AISaveStoryRequest(currentStoryTitle, currentStoryText, currentAudioBase64);
        api.saveAIStory(token, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AIStoryActivity.this, "Đã lưu vào thư viện!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
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

    private void saveAudioToTempFile(String base64) {
        try {
            byte[] data = Base64.decode(base64, Base64.DEFAULT);
            tempAudioFile = File.createTempFile("ai_story", ".mp3", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempAudioFile)) {
                fos.write(data);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void playGeneratedAudio() {
        if (tempAudioFile != null && tempAudioFile.exists()) {
            audioManager.playLocalFile(tempAudioFile.getAbsolutePath(), currentStoryTitle, "AI Gấu Nhỏ");
        }
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
        if (speechRecognizer != null) speechRecognizer.destroy();
    }
}
