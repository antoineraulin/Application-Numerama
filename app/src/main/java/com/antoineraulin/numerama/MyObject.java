package com.antoineraulin.numerama;

public class MyObject {
    private String text;
    private String imageUrl;
    private String link;
    private String date;
    private String unite;

    public MyObject(String text, String imageUrl, String link, String date, String unite) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.link = link;
        this.date = date;
        this.unite = unite;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getLink() {
        return link;
    }
    public String getDate() {
        return date;
    }
    public String getUnite() {
        return unite;
    }

    //getters & setters
}