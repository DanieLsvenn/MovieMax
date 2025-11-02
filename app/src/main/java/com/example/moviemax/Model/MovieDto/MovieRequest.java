package com.example.moviemax.Model.MovieDto;

public class MovieRequest {
    private String title;

    private String genre;

    private int duration;

    private String language;

    private String director;

    private String cast;

    private String description;

    private String posterUrl;

    private String releaseDate;

    private double rating;

    private String showtimeIds;

    public MovieRequest(){}

    public MovieRequest(String title, String genre, int duration, String language, String director, String cast, String description, String posterUrl, String releaseDate, double rating, String showtimeIds) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.language = language;
        this.director = director;
        this.cast = cast;
        this.description = description;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.showtimeIds = showtimeIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getShowtimeIds() {
        return showtimeIds;
    }

    public void setShowtimeIds(String showtimeIds) {
        this.showtimeIds = showtimeIds;
    }
}
