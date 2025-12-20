// BookRepository.java
package com.example.audiobook_for_kids.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.audiobook_for_kids.api.ApiClient;
import com.example.audiobook_for_kids.api.ApiService;
import com.example.audiobook_for_kids.model.Book;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRepository {
    // ...existing code...
    private static BookRepository instance;
    private ApiService apiService;
    private MutableLiveData<List<Book>> booksLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();
    private List<Book> cache = null;

    private BookRepository() {
        apiService = ApiClient.getApiService();
    }

    public static synchronized BookRepository getInstance() {
        if (instance == null) instance = new BookRepository();
        return instance;
    }

    public LiveData<List<Book>> getBooksLiveData() { return booksLiveData; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void fetchBooks() {
        if (cache != null && !cache.isEmpty()) {
            booksLiveData.postValue(cache);
            return;
        }
        loading.postValue(true);
        apiService.getBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                loading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    cache = response.body();
                    booksLiveData.postValue(cache);
                } else {
                    error.postValue("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                loading.postValue(false);
                error.postValue(t.getMessage());
            }
        });
    }
}

