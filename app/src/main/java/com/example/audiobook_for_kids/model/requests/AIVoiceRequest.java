package com.example.audiobook_for_kids.model.requests;

public class AIVoiceRequest {
    private String audioBase64;

    public AIVoiceRequest(String audioBase64) {
        this.audioBase64 = audioBase64;
    }

    public String getAudioBase64() {
        return audioBase64;
    }
}
