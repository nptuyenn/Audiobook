// File: model/Book.java
package com.example.audiobook_for_kids.model;

public class Book {
    private String _id;
    private String title;
    private String author;
    private String coverUrl;
    private String description;
    private String category;

    // Constructor, getter, setter
    public Book() {}

    public String getId() { return _id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverUrl() { return coverUrl; }
    public String getDescription() { return description; }
}