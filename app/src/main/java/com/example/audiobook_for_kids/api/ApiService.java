// File: api/ApiService.java
package com.example.audiobook_for_kids.api;

import com.example.audiobook_for_kids.model.AudioChapter;
import com.example.audiobook_for_kids.model.Book;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface ApiService {
    @GET("books")
    Call<List<Book>> getBooks();

    @GET("audio/book/{bookId}")
    Call<List<AudioChapter>> getChapters(@Path("bookId") String bookId);
}