package com.example.moviemax.Model.DateItemModel;

public class DateItem {
    private String day;
    private int date;
    private String fullDate;
    private boolean isSelected;

    public DateItem(String day, int date, String fullDate) {
        this.day = day;
        this.date = date;
        this.fullDate = fullDate;
        this.isSelected = false;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
