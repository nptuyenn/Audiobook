package com.example.audiobook_for_kids;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

// Added imports
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.audiobook_for_kids.adapter.ChapterAdapter;
import com.example.audiobook_for_kids.model.AudioChapter;
import com.example.audiobook_for_kids.repository.AudioRepository;
import com.example.audiobook_for_kids.repository.UserActivityRepository;
import com.example.audiobook_for_kids.service.AudioPlaybackManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private ConstraintLayout rootLayout;
    private ImageButton btnCollapse, btnMoreOptions, btnFavorite;
    private ImageButton btnPrev, btnNext, btnReplay10, btnForward10;
    private FloatingActionButton btnPlayPause;
    private ImageView ivPlayerCover;
    private TextView tvPlayerTitle, tvPlayerAuthor;
    private SeekBar sbProgress;
    private TextView tvCurrentTime, tvTotalTime;
    private TextView btnChapters, btnSpeed;
    private TextView tvHeaderTitle;
    // --- Logic & Data ---
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBar;
    private boolean isPlaying = false;
    private float currentSpeed = 1.0f;

    private int defaultBackgroundColor;

    // audio url from intent
    private String audioUrl = null;
    private String bookId = null;
    private boolean shouldAutoPlay = false;

    // repository & adapter
    private AudioRepository audioRepository;
    private UserActivityRepository activityRepo;
    private ArrayList<AudioChapter> chapters = new ArrayList<>();
    private AudioPlaybackManager audioManager;
    private boolean isFromMiniPlayer = false;
    private boolean isUserDragging = false;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "AudiobookPrefs";
    private static final String KEY_RECENT = "recent_books"; // comma separated
    private static final int MAX_RECENT = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        defaultBackgroundColor = Color.parseColor("#FFFDF5");

        initViews();

        audioManager = AudioPlaybackManager.getInstance();
        audioManager.initialize(getApplicationContext());
        // LƯU Ý: load intent trước khi khởi tạo MediaPlayer để có thể dùng audioUrl

        loadDataFromIntent();
        setupMediaPlayer();
        setupClickListeners();

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Setup repository to fetch chapters for this book (if bookId provided)
        audioRepository = AudioRepository.getInstance();
        audioRepository.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        audioRepository.getChaptersLiveData().observe(this, list -> {
            if (list != null) {
                chapters.clear();
                chapters.addAll(list);
            }
        });

        activityRepo = UserActivityRepository.getInstance(this);
        activityRepo.getError().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        if (bookId != null && !bookId.isEmpty()) {
            audioRepository.fetchChapters(bookId);
        }

        // Initialize AudioPlaybackManager
        audioManager = AudioPlaybackManager.getInstance();
        audioManager.initialize(this);

        if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
            syncWithAudioManager();
            shouldAutoPlay = false;
        } else {
            if (audioManager.hasActivePlayback()) {
                // Chỉ stop nếu đang KHÔNG phải từ mini player
                if (!isFromMiniPlayer) {
                    audioManager.stop();
                }
            }
        }
    }

    private void initViews() {
        rootLayout = findViewById(R.id.layout_player_root); // ID đã thêm ở bước trước
        btnCollapse = findViewById(R.id.btn_collapse);
        btnMoreOptions = findViewById(R.id.btn_more_options);
        btnFavorite = findViewById(R.id.btn_player_favorite);
        tvHeaderTitle = findViewById(R.id.tv_header_title);

        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnReplay10 = findViewById(R.id.btn_replay_10);
        btnForward10 = findViewById(R.id.btn_forward_10);
        btnPlayPause = findViewById(R.id.btn_play_pause);

        ivPlayerCover = findViewById(R.id.iv_player_cover);
        tvPlayerTitle = findViewById(R.id.tv_player_title);
        tvPlayerAuthor = findViewById(R.id.tv_player_author);

        sbProgress = findViewById(R.id.sb_progress);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        btnChapters = findViewById(R.id.btn_chapters);
        btnSpeed = findViewById(R.id.btn_speed);
    }

    private void setupMediaPlayer() {

        // If opened from mini player and AudioManager has active playback, don't create new MediaPlayer
        if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
            return;
        }

        mediaPlayer = new MediaPlayer();

        try {
            // Set audio attributes for better audio playback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            // Set volume to maximum for clear audio
            mediaPlayer.setVolume(1.0f, 1.0f);

            String source = audioUrl != null && !audioUrl.isEmpty() ? audioUrl : "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                tvTotalTime.setText(formatTime(mp.getDuration()));
                sbProgress.setMax(mp.getDuration());

                // Auto-play nếu được yêu cầu từ detail page
                if (shouldAutoPlay) {
                    playAudio();
                    shouldAutoPlay = false; // Reset flag sau khi auto-play
                } else {
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Runnable để cập nhật thanh seekbar mỗi giây
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    sbProgress.setProgress(currentPos);
                    tvCurrentTime.setText(formatTime(currentPos));
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }



    private boolean isUserDraggingSeekBar() {
        return isUserDragging;
    }

    private void setupAudioManagerProgressUpdater() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (audioManager.hasActivePlayback() && isFromMiniPlayer) {
                    Boolean playing = audioManager.getIsPlaying().getValue();
                    if (playing != null && playing) {
                        int currentPos = audioManager.getCurrentPositionValue();
                        if (!isUserDragging) {
                            sbProgress.setProgress(currentPos);
                            tvCurrentTime.setText(formatTime(currentPos));
                        }
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        };

        // Always start the updater if there's active playback and we're from mini player
        if (audioManager.hasActivePlayback() && isFromMiniPlayer) {
            handler.removeCallbacks(updateSeekBar);
            handler.post(updateSeekBar);
        }
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();

        // Lấy dữ liệu text (Chú ý Key phải khớp với bên gửi)
        String title = intent.getStringExtra("book_title");
        String author = intent.getStringExtra("book_author");
        String coverUrl = intent.getStringExtra("book_cover"); // Hoặc "book_cover_url"
        String passedAudioUrl = intent.getStringExtra("audio_url");
        String passedBookId = intent.getStringExtra("book_id");

        if (passedAudioUrl != null && !passedAudioUrl.isEmpty()) {
            audioUrl = passedAudioUrl;
        }

        if (passedBookId != null) {
            bookId = passedBookId;
        }

        // Check if opened from mini player
        isFromMiniPlayer = intent.getBooleanExtra("from_mini_player", false);

        // Check if should auto-play (từ detail page)
        shouldAutoPlay = intent.getBooleanExtra("auto_play", false);

        tvPlayerTitle.setText(title != null ? title : "Đang tải...");
        tvPlayerAuthor.setText(author != null ? author : "KidoBook");

        // --- XỬ LÝ ẢNH BÌA & PALETTE API ---
        if (coverUrl != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(coverUrl)
                    .placeholder(R.color.teal_200)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // 1. Set ảnh vào View
                            ivPlayerCover.setImageBitmap(resource);

                            // 2. Tạo màu nền Gradient dựa trên ảnh
                            createPaletteBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) { }
                    });
        }
    }

    // Hàm tạo hiệu ứng nền Gradient và đổi màu chữ
    private void createPaletteBackground(Bitmap bitmap) {
        Palette.from(bitmap).generate(palette -> {
            if (palette == null) return;

            int dominantColor = palette.getDominantColor(defaultBackgroundColor);

            // Tạo gradient nền
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {dominantColor, defaultBackgroundColor}
            );
            rootLayout.setBackground(gradient);

            // --- XỬ LÝ MÀU SẮC CONTRAST (TƯƠNG PHẢN) ---
            boolean isDark = isColorDark(dominantColor);

            // 1. Xác định màu chữ và màu icon
            int titleColor = isDark ? Color.WHITE : Color.parseColor("#222222");
            int subTextColor = isDark ? Color.parseColor("#DDDDDD") : Color.parseColor("#666666");
            int iconColor = isDark ? Color.WHITE : Color.parseColor("#333333"); // Màu icon (Trắng hoặc Đen xám)
            int headerColor = isDark ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888"); // Màu chữ "ĐANG PHÁT"

            // 2. Áp dụng màu cho Text
            tvPlayerTitle.setTextColor(titleColor);
            tvPlayerAuthor.setTextColor(subTextColor);


            // Cập nhật màu cho dòng "ĐANG PHÁT"
            if (tvHeaderTitle != null) {
                tvHeaderTitle.setTextColor(headerColor);
            }

            // 3. Áp dụng màu cho Icon (Quan trọng!)
            // Dùng setColorFilter để tô màu lại cho ảnh icon
            btnCollapse.setColorFilter(iconColor);
            btnMoreOptions.setColorFilter(iconColor);


        });
    }

    // Hàm phụ trợ để đổi màu icon trên nút Speed và Chapters
    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setTint(color);
            }
        }
    }

    // Hàm kiểm tra màu tối hay sáng
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    private void setupClickListeners() {
        btnCollapse.setOnClickListener(v -> {
            // Nếu đang dùng Player cục bộ (không phải MiniPlayer) thì chuyển giao
            if (!isFromMiniPlayer && mediaPlayer != null) {
                transferToAudioManager();
            }
            // Nếu đang là MiniPlayer thì chỉ cần hiện lại MiniPlayer bên dưới
            else if (isFromMiniPlayer) {
                audioManager.showMiniPlayer();
            }

            finish();
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
        });

        // Xử lý Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
                if (isPlaying) {
                    audioManager.pause();
                } else {
                    audioManager.play();
                }
            } else if (mediaPlayer != null) {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    playAudio();
                }
            }
        });

        // Xử lý Tua lại 10s
        btnReplay10.setOnClickListener(v -> {
            if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
                int current = audioManager.getCurrentPositionValue();
                audioManager.seekTo(Math.max(0, current - 10000));
                updateUIImmediate();
            } else if (mediaPlayer != null) {
                int current = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(Math.max(0, current - 10000));
                updateUIImmediate();
            }
        });

        // Xử lý Tua đi 10s
        btnForward10.setOnClickListener(v -> {
            if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
                int current = audioManager.getCurrentPositionValue();
                int duration = audioManager.getDurationValue();
                audioManager.seekTo(Math.min(duration, current + 10000));
                updateUIImmediate();
            } else if (mediaPlayer != null) {
                int current = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(Math.min(mediaPlayer.getDuration(), current + 10000));
                updateUIImmediate();
            }
        });

        // Xử lý kéo thanh Seekbar
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserDragging = true;
                handler.removeCallbacks(updateSeekBar); // Ngừng update khi đang kéo
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserDragging = false;
                // Handle seeking for both local MediaPlayer and AudioPlaybackManager
                if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
                    audioManager.seekTo(seekBar.getProgress());
                } else if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                if (isPlaying) handler.post(updateSeekBar); // Tiếp tục update
            }
        });

        // Xử lý nút Tốc độ
        btnSpeed.setOnClickListener(v -> showSpeedDialog());

        // Xử lý nút Chương -> show bottom sheet list
        btnChapters.setOnClickListener(v -> showChaptersBottomSheet());
    }

    private void playAudio() {
        mediaPlayer.start();
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause); // Đổi icon thành Pause
        handler.post(updateSeekBar);

        // Save this book as recently played
        if (bookId != null && !bookId.isEmpty()) {
            addRecentBookId(bookId);
        }
    }

    private void addRecentBookId(String id) {
        String raw = prefs.getString(KEY_RECENT, "");
        LinkedList<String> items = new LinkedList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            String[] parts = raw.split(",");
            for (String p : parts) {
                String t = p.trim();
                if (!t.isEmpty() && !t.equals(id)) items.add(t);
            }
        }
        // add to front
        items.addFirst(id);
        // trim
        while (items.size() > MAX_RECENT) items.removeLast();
        // join
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : items) {
            if (!first) sb.append(",");
            sb.append(s);
            first = false;
        }
        prefs.edit().putString(KEY_RECENT, sb.toString()).apply();
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play_arrow); // Đổi icon thành Play
        handler.removeCallbacks(updateSeekBar);
    }

    private void updateUIImmediate() {
        if (isFromMiniPlayer && audioManager.hasActivePlayback()) {
            int currentPos = audioManager.getCurrentPositionValue();
            sbProgress.setProgress(currentPos);
            tvCurrentTime.setText(formatTime(currentPos));
        } else if (mediaPlayer != null) {
            sbProgress.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
        }
    }

    private void showSpeedDialog() {
        // Chỉ hỗ trợ Android 6.0 (API 23) trở lên
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "Tính năng này chỉ hỗ trợ Android 6.0 trở lên", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] speeds = {"0.75x", "1.0x", "1.25x", "1.5x", "2.0x"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn tốc độ phát")
                .setItems(speeds, (dialog, which) -> {
                    String selected = speeds[which];
                    btnSpeed.setText(selected);

                    // Cắt chữ "x" để lấy số float (VD: "1.5x" -> 1.5f)
                    float speedValue = Float.parseFloat(selected.replace("x", ""));

                    if (mediaPlayer != null) {
                        try {
                            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speedValue));
                            currentSpeed = speedValue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Send progress to backend when user leaves
        if (mediaPlayer != null && bookId != null) {
            activityRepo.updateProgress(bookId, 1, mediaPlayer.getCurrentPosition());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Send final progress only if we still have media player (not transferred)
        if (mediaPlayer != null) {
            if (bookId != null) {
                activityRepo.updateProgress(bookId, 1, mediaPlayer.getCurrentPosition());
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }

    // New: show bottom sheet with chapter list
    private void showChaptersBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_chapters, null);
        RecyclerView rvChapters = sheet.findViewById(R.id.rv_chapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        ChapterAdapter adapter = new ChapterAdapter(this, chapters, chapter -> {
            // When chapter selected: change media source and play
            try {
                if (mediaPlayer != null) {
                    // Save current progress before switching
                    if (bookId != null) activityRepo.updateProgress(bookId, chapter.getChapter(), mediaPlayer.getCurrentPosition());

                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(chapter.getAudioUrl());
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> {
                        playAudio();
                        tvTotalTime.setText(formatTime(mp.getDuration()));
                        sbProgress.setMax(mp.getDuration());
                    });
                    // update displayed title/author if desired
                    tvPlayerTitle.setText(chapter.getTitle());

                    // mark this book as recent when switching chapters
                    if (bookId != null && !bookId.isEmpty()) addRecentBookId(bookId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể phát chương: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });
        rvChapters.setAdapter(adapter);

        // If chapters list empty, show a placeholder message
        dialog.setContentView(sheet);
        dialog.show();
    }

    private void transferToAudioManager() {
        try {
            if (mediaPlayer != null && audioUrl != null && !audioUrl.isEmpty()) {

                int currentPos = mediaPlayer.getCurrentPosition();
                boolean isPlayingLocal = mediaPlayer.isPlaying(); // Kiểm tra xem đang phát hay đang dừng

                String title = tvPlayerTitle.getText().toString();
                String author = tvPlayerAuthor.getText().toString();
                String cover = getIntent().getStringExtra("book_cover"); // Hoặc biến toàn cục nếu có

                android.util.Log.d("PlayerActivity", "Handoff: Pos=" + currentPos + " Playing=" + isPlayingLocal);

                // 2. Gửi sang Manager
                audioManager.setStartPosition(currentPos);

                audioManager.setAudioSource(audioUrl, title, author, cover, bookId);

                mediaPlayer.release();
                mediaPlayer = null;

                handler.removeCallbacks(updateSeekBar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncWithAudioManager() {

        // Hide mini player since we're showing the full player
        audioManager.hideMiniPlayer();

        // Get current state from AudioPlaybackManager
        Boolean playingState = audioManager.getIsPlaying().getValue();
        Integer currentPos = audioManager.getCurrentPosition().getValue();
        Integer duration = audioManager.getDuration().getValue();

        if (playingState != null) {
            this.isPlaying = playingState;
            btnPlayPause.setImageResource(playingState ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }

        // Always use direct values for immediate sync
        if (audioManager.hasActivePlayback()) {
            int dur = audioManager.getDurationValue();
            int pos = audioManager.getCurrentPositionValue();
            if (dur > 0) {
                sbProgress.setMax(dur);
                sbProgress.setProgress(pos);
                tvCurrentTime.setText(formatTime(pos));
                tvTotalTime.setText(formatTime(dur));
            }
        }

        // Fallback to LiveData values if direct values didn't work
        if (currentPos != null && duration != null && duration > 0) {
            sbProgress.setMax(duration);
            sbProgress.setProgress(currentPos);
            tvCurrentTime.setText(formatTime(currentPos));
            tvTotalTime.setText(formatTime(duration));
        }

        // Set up observers for continuous updates
        setupAudioManagerObservers();
    }

    private void setupAudioManagerObservers() {
        // Observe AudioPlaybackManager state for real-time UI updates
        audioManager.getIsPlaying().observe(this, playing -> {
            if (playing != null && isFromMiniPlayer) {
                this.isPlaying = playing;
                btnPlayPause.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

                // Always restart progress updater when state changes
                handler.removeCallbacks(updateSeekBar);
                if (audioManager.hasActivePlayback()) {
                    handler.post(updateSeekBar);
                }
            }
        });

        audioManager.getCurrentPosition().observe(this, position -> {
            if (position != null && isFromMiniPlayer && !isUserDraggingSeekBar()) {
                sbProgress.setProgress(position);
                tvCurrentTime.setText(formatTime(position));
            }
        });

        audioManager.getDuration().observe(this, duration -> {
            if (duration != null && duration > 0 && isFromMiniPlayer) {
                sbProgress.setMax(duration);
                tvTotalTime.setText(formatTime(duration));
            }
        });

        // Start progress updates immediately if there's active playback
        setupAudioManagerProgressUpdater();
    }


}