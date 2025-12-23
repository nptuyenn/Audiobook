// File: model/Book.java
package com.example.audiobook_for_kids.model;

import com.google.gson.annotations.SerializedName;

public class Book {
    @SerializedName("_id")
    private String id;
    
    private String title;
    private String author;
    private String coverUrl = "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766407370/AI_cover_b69ymc.jpg";

    @SerializedName(value = "description", alternate = {"storyText"})
    private String description;
    
    private String category;
    
    @SerializedName(value = "audioUrl", alternate = {"fileUrl"})
    private String audioUrl;

    private String createdAt;

    private int totalListens = 0;
    private float avgRating = 0f;

    // UI state fields
    private boolean isFavorite = false;
    private boolean isAi = false;
    
    @SerializedName(value = "isFinished", alternate = {"hasBeenRead"})
    private boolean isFinished = false; // Thêm trường isFinished

    public Book() {
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    
    public String getAuthor() { 
        return (author == null || author.isEmpty()) ? "AI Gen" : author; 
    }
    
    public String getCoverUrl() { 
        return (coverUrl == null || coverUrl.isEmpty()) ? "" : coverUrl; 
    }
    
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getAudioUrl() { return audioUrl; }
    public String getCreatedAt() { return createdAt; }
    
    public int getTotalListens() { return totalListens; }
    public float getAvgRating() { return avgRating; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public boolean isAi() { return isAi; }
    public void setAi(boolean ai) { isAi = ai; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public void setAvgRating(float avgRating) { this.avgRating = avgRating; }
    public void setTotalListens(int totalListens) { this.totalListens = totalListens; }
    public void setCategory(String category) { this.category = category; }
    public void setAuthor(String author) { this.author = author; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
