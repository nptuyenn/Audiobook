package com.example.audiobook_for_kids.model.requests;

import com.google.gson.annotations.SerializedName;

public class AISaveStoryRequest {
    private String title;
    private String text;
    
    @SerializedName("audioUrl")
    private String audioUrl;

    public AISaveStoryRequest(String title, String text, String audioUrl) {
        this.title = title;
        this.text = text;
        this.audioUrl = audioUrl;
    }

    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getAudioUrl() { return audioUrl; }
}
