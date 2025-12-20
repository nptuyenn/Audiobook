package com.example.audiobook_for_kids.model.requests;

import java.util.List;

public class AIChatRequest {
    private String message;
    private List<String> history;

    public AIChatRequest(String message, List<String> history) {
        this.message = message;
        this.history = history;
    }

    public String getMessage() { return message; }
    public List<String> getHistory() { return history; }
}
