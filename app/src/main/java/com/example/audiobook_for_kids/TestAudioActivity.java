package com.example.audiobook_for_kids;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.model.AudioChapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestAudioActivity extends AppCompatActivity {

    private Button btnLoad, btnPlay;
    private MediaPlayer player;
    private String testAudioUrl = null;  // audio sẽ lấy từ server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio);

        btnLoad = findViewById(R.id.btnLoadAudio);
        btnPlay = findViewById(R.id.btnPlayAudio);

        btnLoad.setOnClickListener(v -> loadAudioFromServer());

        btnPlay.setOnClickListener(v -> playAudio());
    }

    private void loadAudioFromServer() {
        ApiService api = ApiClient.getClient().create(ApiService.class);

        // FIX: bookId bạn nhập thủ công để test
        String testBookId = "693aebe7f7ed43166386341b";

        api.getChapters(testBookId).enqueue(new Callback<List<AudioChapter>>() {
            @Override
            public void onResponse(Call<List<AudioChapter>> call, Response<List<AudioChapter>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Lấy chương đầu tiên để test
                    AudioChapter chap = response.body().get(0);
                    testAudioUrl = chap.getAudioUrl();

                    Toast.makeText(TestAudioActivity.this,
                            "Đã lấy URL: " + testAudioUrl,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AudioChapter>> call, Throwable t) {
                Toast.makeText(TestAudioActivity.this, "Lỗi API", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void playAudio() {
        if (testAudioUrl == null) {
            Toast.makeText(this, "Chưa load audio từ server!", Toast.LENGTH_SHORT).show();
            return;
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(testAudioUrl);
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(TestAudioActivity.this, "Đang phát audio", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không phát được", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) player.release();
        super.onDestroy();
    }
}
