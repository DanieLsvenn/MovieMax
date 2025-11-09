package com.example.moviemax.Model.MovieDTO;

public class MovieResponse {
    private long id;
    private String title;
    private String genre;
    private int duration;
    private String language;
    private String director;
    private String cast;
    private String description;
    private String posterPath;
    private String releaseDate;
    private double rating;

    // getter/setter đầy đủ
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getCast() { return cast; }
    public void setCast(String cast) { this.cast = cast; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
