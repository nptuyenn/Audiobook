// File: api/ApiService.java
package com.example.audiobook_for_kids.api;

import com.example.audiobook_for_kids.model.AudioChapter;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.model.requests.AIChatRequest;
import com.example.audiobook_for_kids.model.requests.AIVoiceRequest;
import com.example.audiobook_for_kids.model.requests.AISaveStoryRequest;
import com.example.audiobook_for_kids.model.requests.AIStoryRequest;
import com.example.audiobook_for_kids.model.requests.FavoriteRequest;
import com.example.audiobook_for_kids.model.requests.ProgressRequest;
import com.example.audiobook_for_kids.model.requests.ReviewRequest;
import com.example.audiobook_for_kids.model.requests.StartListeningRequest;
import com.example.audiobook_for_kids.model.responses.AIChatResponse;
import com.example.audiobook_for_kids.model.responses.AIStoryResponse;
import com.example.audiobook_for_kids.model.responses.TranscribeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface ApiService {
    @GET("books")
    Call<List<Book>> getBooks();

    @GET("audio/book/{bookId}")
    Call<List<AudioChapter>> getChapters(@Path("bookId") String bookId);

    // User activity endpoints
    @GET("activity/favorites")
    Call<List<FavoriteBook>> getFavorites(@Header("Authorization") String authHeader);

    @POST("activity/favorites")
    Call<Void> setFavorite(@Header("Authorization") String authHeader, @Body FavoriteRequest body);

    @POST("activity/start-listening")
    Call<Void> startListening(@Header("Authorization") String authHeader, @Body StartListeningRequest body);

    @GET("activity/history")
    Call<List<Book>> getHistory(@Header("Authorization") String authHeader);

    @POST("activity/review")
    Call<Void> submitReview(@Header("Authorization") String authHeader, @Body ReviewRequest body);

    @POST("activity/progress")
    Call<Void> updateProgress(@Header("Authorization") String authHeader, @Body ProgressRequest body);

    @GET("activity/recent")
    Call<List<Book>> getRecentListens(@Header("Authorization") String authHeader);

    // AI endpoints
    @POST("ai/story")
    Call<AIStoryResponse> generateAIStory(@Header("Authorization") String authHeader, @Body AIStoryRequest body);

    @POST("ai/story/save")
    Call<Void> saveAIStory(@Header("Authorization") String authHeader, @Body AISaveStoryRequest body);

    @GET("ai-content/user/{userId}")
    Call<List<Book>> getAISaveStories(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    @POST("ai/chat")
    Call<AIChatResponse> chatWithAI(@Header("Authorization") String authHeader, @Body AIChatRequest body);

    @POST("ai/story/voice")
    Call<TranscribeResponse> generateAIStoryFromVoice(
            @Header("Authorization") String authHeader, 
            @Body AIVoiceRequest body
    );

    @POST("ai/story/tts")
    Call<Map<String, String>> generateStoryAudio(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );
}
