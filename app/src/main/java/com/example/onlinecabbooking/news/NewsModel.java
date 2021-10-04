package com.example.onlinecabbooking.news;

public class NewsModel {
    public String title, source, detail, url,image;

    public NewsModel(String title, String source, String detail, String url, String image) {
        this.title = title;
        this.source = source;
        this.detail = detail;
        this.url = url;
        this.image = image;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

