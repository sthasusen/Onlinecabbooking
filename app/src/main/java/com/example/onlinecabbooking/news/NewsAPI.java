package com.example.onlinecabbooking.news;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NewsAPI {

    @GET("news")
    Call<List<NewsModel>> getNews();

}
