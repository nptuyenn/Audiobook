package com.example.audiobook_for_kids.model.requests;

public class StartListeningRequest {
    private String bookId;

    public StartListeningRequest(String bookId) {
        this.bookId = bookId;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
}
