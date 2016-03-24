package com.example.sanket.model;

import com.example.sanket.dateutils.DateUtil;

/**
 * Created by sanket on 10/10/15.
 */
public class File {

    private boolean favorite;
    private String playlistId;
    private String id;
    private String title;
    private String numberOfViews;
    private String publishedDate;
    private String thumbnailURL;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumberOfViews() {
        return numberOfViews;
    }

    public void setNumberOfViews(String numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

    public String getPublishedDate() {

        return DateUtil.convertDate(this.publishedDate);
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public void setFavorite (boolean favorite) {

        this.favorite = favorite;
    }

    public boolean isFavorite() {

        return this.favorite;
    }

    public void setPlaylistId (String playlistId) {

        this.playlistId = playlistId;
    }

    public String getPlaylistId () {

        return this.playlistId;
    }
}