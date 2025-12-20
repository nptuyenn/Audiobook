// File: model/requests/FavoriteRequest.java
package com.example.audiobook_for_kids.model.requests;

public class FavoriteRequest {
    private String bookId;
    private boolean isFavorite;

    public FavoriteRequest() {}

    public FavoriteRequest(String bookId, boolean isFavorite) {
        this.bookId = bookId;
        this.isFavorite = isFavorite;
    }

    public String getBookId() { return bookId; }
    public boolean isFavorite() { return isFavorite; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}

