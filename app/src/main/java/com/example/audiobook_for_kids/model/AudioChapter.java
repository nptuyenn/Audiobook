// File: model/AudioChapter.java
package com.example.audiobook_for_kids.model;

public class AudioChapter {
    private int chapter;
    private String title;
    private String audioUrl;

    // Constructor mặc định
    public AudioChapter() {
    }

    // Constructor đầy đủ
    public AudioChapter(int chapter, String title, String audioUrl) {
        this.chapter = chapter;
        this.title = title;
        this.audioUrl = audioUrl;
    }

    // Getters
    public int getChapter() { return chapter; }
    public String getTitle() { return title; }
    public String getAudioUrl() { return audioUrl; }

    // Setters
    public void setChapter(int chapter) { this.chapter = chapter; }
    public void setTitle(String title) { this.title = title; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}