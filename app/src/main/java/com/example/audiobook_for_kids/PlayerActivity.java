package com.example.audiobook_for_kids;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        defaultBackgroundColor = Color.parseColor("#FFFDF5");

        initViews();
        setupMediaPlayer();
        loadDataFromIntent();
        setupClickListeners();
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
        mediaPlayer = new MediaPlayer();

        // TODO: Thay thế URL này bằng URL thật từ Intent hoặc API
        try {
            // Link nhạc demo (MP3 online)
            String demoUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
            mediaPlayer.setDataSource(demoUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                tvTotalTime.setText(formatTime(mp.getDuration()));
                sbProgress.setMax(mp.getDuration());
                // Tự động phát khi load xong (tùy chọn)
                // playAudio();
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

    private void loadDataFromIntent() {
        Intent intent = getIntent();

        // Lấy dữ liệu text (Chú ý Key phải khớp với bên gửi)
        String title = intent.getStringExtra("book_title");
        String author = intent.getStringExtra("book_author");
        String coverUrl = intent.getStringExtra("book_cover"); // Hoặc "book_cover_url"

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
            finish();
            // Hiệu ứng: trang cũ giữ nguyên, PlayerActivity trượt xuống
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_down);
        });

        // Xử lý Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer == null) return;
            if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });

        // Xử lý Tua lại 10s
        btnReplay10.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                int current = mediaPlayer.getCurrentPosition();
                mediaPlayer.seekTo(Math.max(0, current - 10000));
                updateUIImmediate();
            }
        });

        // Xử lý Tua đi 10s
        btnForward10.setOnClickListener(v -> {
            if (mediaPlayer != null) {
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
                handler.removeCallbacks(updateSeekBar); // Ngừng update khi đang kéo
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
                if (isPlaying) handler.post(updateSeekBar); // Tiếp tục update
            }
        });

        // Xử lý nút Tốc độ
        btnSpeed.setOnClickListener(v -> showSpeedDialog());

        // Xử lý nút Chương (Placeholder)
        btnChapters.setOnClickListener(v -> {
            // TODO: Hiển thị BottomSheet chứa danh sách chương
            Toast.makeText(this, "Danh sách chương đang cập nhật", Toast.LENGTH_SHORT).show();
        });
    }

    private void playAudio() {
        mediaPlayer.start();
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause); // Đổi icon thành Pause
        handler.post(updateSeekBar);
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play_arrow); // Đổi icon thành Play
        handler.removeCallbacks(updateSeekBar);
    }

    private void updateUIImmediate() {
        if (mediaPlayer != null) {
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
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}