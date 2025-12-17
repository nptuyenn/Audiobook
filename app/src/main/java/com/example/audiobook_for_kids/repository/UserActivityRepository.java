// File: repository/UserActivityRepository.java
package com.example.audiobook_for_kids.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.auth.SessionManager;
import com.example.audiobook_for_kids.model.FavoriteBook;
import com.example.audiobook_for_kids.model.requests.FavoriteRequest;
import com.example.audiobook_for_kids.model.requests.ProgressRequest;
import com.example.audiobook_for_kids.model.requests.ReviewRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivityRepository {
    private static UserActivityRepository instance;
    private ApiService api;
    private SessionManager session;

    private MutableLiveData<List<FavoriteBook>> favoritesLive = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    private UserActivityRepository(Context ctx) {
        api = ApiClient.getApiService();
        session = SessionManager.getInstance(ctx);
    }

    public static synchronized UserActivityRepository getInstance(Context ctx) {
        if (instance == null) instance = new UserActivityRepository(ctx);
        return instance;
    }

    public LiveData<List<FavoriteBook>> getFavoritesLive() { return favoritesLive; }
    public LiveData<String> getError() { return error; }

    public void clearError() { error.postValue(null); }

    private String bearer() {
        String t = session.getToken();
        return t != null ? "Bearer " + t : null;
    }

    public void fetchFavorites() {
        String auth = bearer();
        if (auth == null) {
            // Not logged in: clear favorites silently
            favoritesLive.postValue(null);
            return;
        }
        api.getFavorites(auth).enqueue(new Callback<List<FavoriteBook>>() {
            @Override
            public void onResponse(Call<List<FavoriteBook>> call, Response<List<FavoriteBook>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoritesLive.postValue(response.body());
                } else {
                    // only post error for server failure
                    error.postValue("Không thể tải favorites: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteBook>> call, Throwable t) {
                error.postValue(t.getMessage());
            }
        });
    }

    public void setFavorite(String bookId, boolean isFav, Callback<Void> cb) {
        String auth = bearer();
        if (auth == null) {
            // silently fail: caller should handle UI if user not logged in
            if (cb != null) cb.onResponse(null, Response.success(null));
            return;
        }
        FavoriteRequest req = new FavoriteRequest(bookId, isFav);
        api.setFavorite(auth, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Refresh favorites list
                    fetchFavorites();
                } else {
                    error.postValue("Không thể cập nhật yêu thích: " + response.code());
                }
                if (cb != null) cb.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.postValue(t.getMessage());
                if (cb != null) cb.onFailure(call, t);
            }
        });
    }

    public void submitReview(String bookId, int rating, String review, Callback<Void> cb) {
        String auth = bearer();
        if (auth == null) {
            // silently ignore if not logged in; caller/UI should prompt login
            if (cb != null) cb.onResponse(null, Response.success(null));
            return;
        }
        ReviewRequest req = new ReviewRequest(bookId, rating, review);
        api.submitReview(auth, req).enqueue(cb != null ? cb : new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // no-op
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.postValue(t.getMessage());
            }
        });
    }

    public void updateProgress(String bookId, int chapter, int progressMs) {
        String auth = bearer();
        if (auth == null) return;
        ProgressRequest req = new ProgressRequest(bookId, chapter, progressMs);
        api.updateProgress(auth, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // no-op
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                error.postValue(t.getMessage());
            }
        });
    }
}
