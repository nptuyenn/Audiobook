package com.example.audiobook_for_kids;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.audiobook_for_kids.adapter.ChatAdapter;
import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.auth.SessionManager;
import com.example.audiobook_for_kids.model.ChatMessage;
import com.example.audiobook_for_kids.model.requests.AIChatRequest;
import com.example.audiobook_for_kids.model.responses.AIChatResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private List<String> chatHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_chat);

        messageList = new ArrayList<>();
        chatHistory = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Welcome message
        addMessage("Chào bé! Gấu Nhỏ đây, bé muốn hỏi gì gấu không?", false);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        addMessage(msg, true);
        etMessage.setText("");
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = "Bearer " + SessionManager.getInstance(this).getToken();
        
        AIChatRequest request = new AIChatRequest(msg, chatHistory);

        apiService.chatWithAI(token, request).enqueue(new Callback<AIChatResponse>() {
            @Override
            public void onResponse(Call<AIChatResponse> call, Response<AIChatResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getText();
                    addMessage(answer, false);
                    chatHistory.add("User: " + msg);
                    chatHistory.add("AI: " + answer);
                } else {
                    Toast.makeText(AIChatActivity.this, "Gấu Nhỏ đang bận rồi...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AIChatResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AIChatActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }
}
