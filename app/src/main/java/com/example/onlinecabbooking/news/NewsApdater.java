package com.example.onlinecabbooking.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinecabbooking.R;
import com.example.onlinecabbooking.apiUrl;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsApdater extends RecyclerView.Adapter<NewsApdater.newsViewHolder>{

    Context mcontext;
    List<NewsModel> newsModelList;

    public NewsApdater (Context mcontext, List<NewsModel> newsModelList){
        this.mcontext = mcontext;
        this.newsModelList = newsModelList;
    }

    @NonNull
    @Override
    public newsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_layout,parent,false);
        return new newsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull newsViewHolder holder, int position) {
        NewsModel newsModel = newsModelList.get(position);
        holder.ntitle.setText(newsModel.getDetail());
        holder.nsource.setText(newsModel.getSource());
        holder.ndetail.setText(newsModel.getDetail());
        holder.nurl.setText(newsModel.getUrl());
        String imgpath = apiUrl.imagePath + newsModel.getImage();
        Picasso.get().load(imgpath).into(holder.newsimage);
    }

    @Override
    public int getItemCount() {
        return newsModelList.size();
    }


    public class newsViewHolder extends RecyclerView.ViewHolder{

        ImageView newsimage;
        TextView ntitle,nsource,ndetail,nurl;


        public newsViewHolder(@NonNull View itemView) {
            super(itemView);

            ntitle = itemView.findViewById(R.id.etnewstitle);
            nsource  =itemView.findViewById(R.id.etnewssource);
            ndetail = itemView.findViewById(R.id.etnewssummary);
            nurl = itemView.findViewById(R.id.etnewsurl);
            newsimage = itemView.findViewById(R.id.newsimage);

        }
    }



}
