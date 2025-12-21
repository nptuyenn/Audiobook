package com.example.audiobook_for_kids.service;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AudioPlaybackManager {
    private static AudioPlaybackManager instance;

    private MediaPlayer mediaPlayer;
    private final Handler handler;
    private Runnable progressUpdater;

    // Audio focus management
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    // LiveData for UI updates
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<String> currentTitle = new MutableLiveData<>("");
    private final MutableLiveData<String> currentAuthor = new MutableLiveData<>("");
    private final MutableLiveData<String> currentCover = new MutableLiveData<>("");
    private final MutableLiveData<String> currentBookId = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> shouldShowMiniPlayer = new MutableLiveData<>(false);
    private int startPosition = -1;

    public void setStartPosition(int position) {
        this.startPosition = position;
    }
    private AudioPlaybackManager() {
        handler = new Handler(Looper.getMainLooper());
        setupProgressUpdater();
    }

    public static synchronized AudioPlaybackManager getInstance() {
        if (instance == null) {
            instance = new AudioPlaybackManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        // Initialize audio manager for audio focus management
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Create audio focus request for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
        }
    }

    private void setupProgressUpdater() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    currentPosition.setValue(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    // Audio focus change listener
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // Resume playback
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        play();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Pause playback
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lower the volume (duck)
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(0.3f, 0.3f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    // Restore volume
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(1.0f, 1.0f);
                    }
                    break;
            }
        }
    };

    public void playLocalFile(String filePath, String title, String author) {
        setAudioSource(filePath, title, author, "", "ai_story");
    }

    public void setAudioSource(String audioUrl, String title, String author, String cover, String bookId) {
        try {
            // Dừng bài cũ trước khi phát bài mới
            stopCurrentPlayback();

            if (audioUrl == null || audioUrl.isEmpty()) {
                return;
            }

            mediaPlayer = new MediaPlayer();

            // Cấu hình Audio Attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setDataSource(audioUrl);

            // Cập nhật thông tin bài hát ngay lập tức
            currentTitle.postValue(title != null ? title : "Unknown Title");
            currentAuthor.postValue(author != null ? author : "Unknown Author");
            currentCover.postValue(cover != null ? cover : "");
            currentBookId.postValue(bookId != null ? bookId : "");

            mediaPlayer.setOnPreparedListener(mp -> {
                duration.setValue(mp.getDuration());
                shouldShowMiniPlayer.setValue(true);
                
                if (startPosition >= 0) {
                    mp.seekTo(startPosition);
                    startPosition = -1;
                }
                play();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying.setValue(false);
                currentPosition.setValue(0);
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCurrentPlayback() {
        try {
            handler.removeCallbacks(progressUpdater);
            abandonAudioFocus();
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            isPlaying.setValue(false);
            currentPosition.setValue(0);
            duration.setValue(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                int focusResult = requestAudioFocus();
                if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.start();
                    isPlaying.setValue(true);
                    handler.post(progressUpdater);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int requestAudioFocus() {
        if (audioManager == null) return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) return audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            return audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    public void pause() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying.setValue(false);
                handler.removeCallbacks(progressUpdater);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            currentPosition.setValue(position);
        }
    }

    public void stop() {
        stopCurrentPlayback();
        shouldShowMiniPlayer.setValue(false);
    }

    private void abandonAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest != null) audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(focusChangeListener);
            }
        }
    }

    public void hideMiniPlayer() { shouldShowMiniPlayer.setValue(false); }
    public void showMiniPlayer() { shouldShowMiniPlayer.setValue(true); }

    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Integer> getCurrentPosition() { return currentPosition; }
    public LiveData<Integer> getDuration() { return duration; }
    public LiveData<String> getCurrentTitle() { return currentTitle; }
    public LiveData<String> getCurrentAuthor() { return currentAuthor; }
    public LiveData<String> getCurrentCover() { return currentCover; }
    public LiveData<String> getCurrentBookId() { return currentBookId; }
    public LiveData<Boolean> getShouldShowMiniPlayer() { return shouldShowMiniPlayer; }

    public boolean hasActivePlayback() { return mediaPlayer != null; }
    public int getCurrentPositionValue() { return (mediaPlayer != null) ? mediaPlayer.getCurrentPosition() : 0; }
    public int getDurationValue() { return (mediaPlayer != null) ? mediaPlayer.getDuration() : 0; }
}
