// File: repository/BookRepository.java
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
    private static BookRepository instance;
    private ApiService api;

    private MutableLiveData<List<Book>> booksLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private MutableLiveData<String> error = new MutableLiveData<>();

    private BookRepository() {
        api = ApiClient.getClient().create(ApiService.class);
    }

    public static synchronized BookRepository getInstance() {
        if (instance == null) instance = new BookRepository();
        return instance;
    }

    public LiveData<List<Book>> getBooksLiveData() { return booksLiveData; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void fetchBooks() {
        loading.setValue(true);
        api.getBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    booksLiveData.postValue(response.body());
                } else {
                    error.postValue("Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                loading.setValue(false);
                error.postValue(t.getMessage());
            }
        });
    }

    public void fetchBookById(String id, Callback<Book> cb) {
        api.getChapters(id); // placeholder
        // Normally we'd have getBook(id) in ApiService, but we use search/filter for now
    }
}
