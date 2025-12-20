package com.example.audiobook_for_kids.model.responses;

public class AIStoryResponse {
    private String text;
    private String audioBase64;
    private String audioMime;

    public String getText() { return text; }
    public String getAudioBase64() { return audioBase64; }
    public String getAudioMime() { return audioMime; }
}
