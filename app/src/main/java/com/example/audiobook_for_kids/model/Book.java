// File: model/Book.java
package com.example.audiobook_for_kids.model;

import com.google.gson.annotations.SerializedName;

public class Book {
    @SerializedName("_id")
    private String id;
    private String title;
    private String author;
    private String coverUrl;
    private String description;
    private String category;
    private int totalListens = 0;
    private float avgRating = 0f;

    // UI state field
    private boolean isFavorite = false;

    public Book() {
    }

    public Book(String id, String title, String author, String coverUrl, String description, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.description = description;
        this.category = category;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getTotalListens() { return totalListens; }
    public float getAvgRating() { return avgRating; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setAvgRating(float avgRating) { this.avgRating = avgRating; }
    public void setTotalListens(int totalListens) { this.totalListens = totalListens; }
    public void setCategory(String category) { this.category = category; }
}
