// File: model/requests/ReviewRequest.java
package com.example.audiobook_for_kids.model.requests;

public class ReviewRequest {
    private String bookId;
    private int rating;
    private String review;

    public ReviewRequest() {}

    public ReviewRequest(String bookId, int rating, String review) {
        this.bookId = bookId;
        this.rating = rating;
        this.review = review;
    }

    public String getBookId() { return bookId; }
    public int getRating() { return rating; }
    public String getReview() { return review; }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setReview(String review) { this.review = review; }
}

