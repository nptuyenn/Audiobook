// File: repository/UserActivityRepository.java
package com.example.audiobook_for_kids.repository;

import android.content.Context;
import android.util.Base64;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.auth.SessionManager;
import com.example.audiobook_for_kids.model.Book;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.model.requests.FavoriteRequest;
import com.example.audiobook_for_kids.model.requests.ProgressRequest;
import com.example.audiobook_for_kids.model.requests.ReviewRequest;
import com.example.audiobook_for_kids.model.requests.StartListeningRequest;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivityRepository {
    private static UserActivityRepository instance;
    private ApiService api;
    private SessionManager session;

    private MutableLiveData<List<FavoriteBook>> favoritesLive = new MutableLiveData<>();
    private MutableLiveData<List<Book>> historyLive = new MutableLiveData<>();
    private MutableLiveData<List<Book>> aiStoriesLive = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    private UserActivityRepository(Context ctx) {
        api = ApiClient.getClient().create(ApiService.class);
        session = SessionManager.getInstance(ctx);
    }

    public static synchronized UserActivityRepository getInstance(Context ctx) {
        if (instance == null) instance = new UserActivityRepository(ctx);
        return instance;
    }

    public LiveData<List<FavoriteBook>> getFavoritesLive() { return favoritesLive; }
    public LiveData<List<Book>> getHistoryLive() { return historyLive; }
    public LiveData<List<Book>> getAiStoriesLive() { return aiStoriesLive; }
    public LiveData<String> getError() { return error; }

    public void clearError() { error.postValue(null); }

    private String bearer() {
        String t = session.getToken();
        return t != null ? "Bearer " + t : null;
    }

    private String getUserIdFromToken() {
        try {
            String token = session.getToken();
            if (token == null) return null;
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(Base64.decode(parts[1], Base64.DEFAULT));
            JSONObject json = new JSONObject(payload);
            return json.getString("id");
        } catch (Exception e) {
            return null;
        }
    }

    public void startListening(String bookId) {
        String auth = bearer();
        if (auth == null || bookId == null) return;
        
        api.startListening(auth, new StartListeningRequest(bookId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) fetchHistory();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void fetchHistory() {
        String auth = bearer();
        if (auth == null) return;
        api.getHistory(auth).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> historyBooks = response.body();
                    List<Book> allBooks = BookRepository.getInstance().getBooksLiveData().getValue();
                    
                    if (allBooks != null && !allBooks.isEmpty()) {
                        for (Book h : historyBooks) {
                            for (Book b : allBooks) {
                                if (b.getId().equals(h.getId())) {
                                    // Đồng bộ Rating và AudioUrl từ danh sách tổng
                                    h.setAvgRating(b.getAvgRating());
                                    h.setAudioUrl(b.getAudioUrl());
                                    h.setAi(b.isAi());
                                    break;
                                }
                            }
                            h.setFinished(true);
                        }
                    }
                    historyLive.postValue(historyBooks);
                }
            }
            @Override public void onFailure(Call<List<Book>> call, Throwable t) { error.postValue(t.getMessage()); }
        });
    }

    public void fetchAIStories() {
        String auth = bearer();
        String userId = getUserIdFromToken();
        if (auth == null || userId == null) return;
        api.getAISaveStories(auth, userId).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> stories = response.body();
                    for (Book b : stories) b.setAi(true);
                    aiStoriesLive.postValue(stories);
                }
            }
            @Override public void onFailure(Call<List<Book>> call, Throwable t) { error.postValue(t.getMessage()); }
        });
    }

    public void fetchFavorites() {
        String auth = bearer();
        if (auth == null) return;
        api.getFavorites(auth).enqueue(new Callback<List<FavoriteBook>>() {
            @Override
            public void onResponse(Call<List<FavoriteBook>> call, Response<List<FavoriteBook>> response) {
                if (response.isSuccessful()) favoritesLive.postValue(response.body());
            }
            @Override public void onFailure(Call<List<FavoriteBook>> call, Throwable t) { error.postValue(t.getMessage()); }
        });
    }

    public void setFavorite(String bookId, boolean isFav, Callback<Void> cb) {
        String auth = bearer();
        if (auth == null) return;
        FavoriteRequest req = new FavoriteRequest(bookId, isFav);
        api.setFavorite(auth, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) fetchFavorites();
                if (cb != null) cb.onResponse(call, response);
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { if (cb != null) cb.onFailure(call, t); }
        });
    }

    public void updateProgress(String bookId, int chapter, int progressMs) {
        String auth = bearer();
        if (auth == null || bookId == null) return;
        ProgressRequest req = new ProgressRequest(bookId, chapter, progressMs);
        api.updateProgress(auth, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Định kỳ cập nhật lại danh sách lịch sử để tab "Đã nghe" luôn mới
                if (response.isSuccessful()) fetchHistory();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void submitReview(String bookId, int rating, String review, Callback<Void> cb) {
        String auth = bearer();
        if (auth == null) return;
        ReviewRequest req = new ReviewRequest(bookId, rating, review);
        api.submitReview(auth, req).enqueue(cb);
    }
}
