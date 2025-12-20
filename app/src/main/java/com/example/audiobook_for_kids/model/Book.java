// File: model/Book.java
package com.example.audiobook_for_kids.model;

public class Book {
    private String _id;
    private String title;
    private String author;
    private String coverUrl;
    private String description;
    private String category;

    // New fields to track user activity / display
    private boolean isFavorite = false;
    private float avgRating = 0f;

    // Constructor mặc định (cần thiết cho Firebase và Serialization)
    public Book() {
    }

    // Constructor đầy đủ
    public Book(String _id, String title, String author, String coverUrl, String description, String category) {
        this._id = _id;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.description = description;
        this.category = category;
    }

    public String getId() { return _id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getDescription() { return description; }

    // New getters/setters
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public float getAvgRating() { return avgRating; }
    public void setAvgRating(float avgRating) { this.avgRating = avgRating; }
}