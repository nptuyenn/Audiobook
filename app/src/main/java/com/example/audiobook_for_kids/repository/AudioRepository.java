// AudioRepository.java
package com.example.audiobook_for_kids.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.model.AudioChapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioRepository {
    // ...existing code...
    private static AudioRepository instance;
    private ApiService apiService;
    private MutableLiveData<List<AudioChapter>> chaptersLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private Map<String, List<AudioChapter>> cache = new HashMap<>();

    private AudioRepository() {
        apiService = ApiClient.getApiService();
    }

    public static synchronized AudioRepository getInstance() {
        if (instance == null) instance = new AudioRepository();
        return instance;
    }

    public LiveData<List<AudioChapter>> getChaptersLiveData() { return chaptersLiveData; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void fetchChapters(String bookId) {
        if (cache.containsKey(bookId)) {
            chaptersLiveData.postValue(cache.get(bookId));
            return;
        }
        loading.postValue(true);
        apiService.getChapters(bookId).enqueue(new Callback<List<AudioChapter>>() {
            @Override
            public void onResponse(Call<List<AudioChapter>> call, Response<List<AudioChapter>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    cache.put(bookId, response.body());
                    chaptersLiveData.postValue(response.body());
                } else {
                    error.postValue("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AudioChapter>> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t.getMessage());
            }
        });
    }
}

