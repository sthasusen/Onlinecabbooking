package com.example.onlinecabbooking.news;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinecabbooking.MainActivity;
import com.example.onlinecabbooking.R;
import com.example.onlinecabbooking.StrictClassMode;
import com.example.onlinecabbooking.apiUrl;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    float x1, y1, x2, y2;
    RecyclerView newRecycleview;
    List<NewsModel> newsModellist;
    NewsApdater newsApdater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        newRecycleview = findViewById(R.id.newsrecycleview);
        fetchnews();

    }

    private void fetchnews() {
        StrictClassMode.StrictMode();
        newsModellist = new ArrayList<>();
        NewsAPI newsAPI = apiUrl.getInstance().create(NewsAPI.class);
        Call<List<NewsModel>> NewslistCall = newsAPI.getNews();
        try {
            Response<List<NewsModel>> newResponse = NewslistCall.execute();
            newsModellist = newResponse.body();
            newsApdater = new NewsApdater(this, newsModellist);
            newRecycleview.setAdapter(newsApdater);
            newRecycleview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2) {
                    Intent i = new Intent(NewsActivity.this, MainActivity.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }

}