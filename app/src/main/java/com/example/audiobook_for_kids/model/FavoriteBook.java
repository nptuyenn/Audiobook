// File: model/FavoriteBook.java
package com.example.audiobook_for_kids.model;

public class FavoriteBook {
    private String bookId;
    private String title;
    private String coverUrl;
    private String author;

    public FavoriteBook() {}

    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public String getAuthor() { return author; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setTitle(String title) { this.title = title; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setAuthor(String author) { this.author = author; }
}

