package com.example.audiobook_for_kids.model.requests;

public class AISaveStoryRequest {
    private String title;
    private String text;
    private String audioBase64;

    public AISaveStoryRequest(String title, String text, String audioBase64) {
        this.title = title;
        this.text = text;
        this.audioBase64 = audioBase64;
    }

    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getAudioBase64() { return audioBase64; }
}
