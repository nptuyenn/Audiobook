// File: model/requests/ProgressRequest.java
package com.example.audiobook_for_kids.model.requests;

public class ProgressRequest {
    private String bookId;
    private int chapter;
    private int progressTime; // milliseconds

    public ProgressRequest() {}

    public ProgressRequest(String bookId, int chapter, int progressTime) {
        this.bookId = bookId;
        this.chapter = chapter;
        this.progressTime = progressTime;
    }

    public String getBookId() { return bookId; }
    public int getChapter() { return chapter; }
    public int getProgressTime() { return progressTime; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setChapter(int chapter) { this.chapter = chapter; }
    public void setProgressTime(int progressTime) { this.progressTime = progressTime; }
}

