package com.example.audiobook_for_kids.model.requests;

public class AIStoryRequest {
    private String topic;
    private String title;

    public AIStoryRequest(String topic, String title) {
        this.topic = topic;
        this.title = title;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
